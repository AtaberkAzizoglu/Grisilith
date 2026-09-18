param(
    [Parameter(Mandatory=$true)][string]$InputPath,
    [Parameter(Mandatory=$true)][string]$OutputPath,
    [Parameter(Mandatory=$true)][int[]]$Columns,
    [Parameter(Mandatory=$true)][int[]]$Rows
)

# Mechanical game-atlas import only: keep generated artwork, remove its
# neutral checkerboard matte, and align the original frames on a feet pivot.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies System.Drawing.Common,System.Drawing.Primitives,System.Private.Windows.GdiPlus,System.Private.Windows.Core,System.Console,System.Collections -TypeDefinition @'
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;

public static class CharacterSheetImport {
    public static void Prepare(string input, string output, int[] columns, int[] rows) {
        using (var source = new Bitmap(input))
        using (var clean = new Bitmap(source.Width, source.Height, PixelFormat.Format32bppArgb)) {
            int width = source.Width, height = source.Height, count = width * height;
            var pixels = new Color[count];
            var candidate = new bool[count];
            var visited = new bool[count];
            var removed = new bool[count];
            var queue = new int[count];
            for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
                int index = y * width + x;
                Color c = source.GetPixel(x, y);
                pixels[index] = c;
                int min = Math.Min(c.R, Math.Min(c.G, c.B));
                int max = Math.Max(c.R, Math.Max(c.G, c.B));
                candidate[index] = c.A == 0 || (min >= 150 && max - min <= 30);
            }
            for (int seed = 0; seed < count; seed++) {
                if (!candidate[seed] || visited[seed]) continue;
                int head = 0, tail = 1; queue[0] = seed; visited[seed] = true;
                bool exterior = false;
                while (head < tail) {
                    int index = queue[head++], x = index % width, y = index / width;
                    exterior |= x == 0 || y == 0 || x == width - 1 || y == height - 1;
                    int[] neighbors = { x > 0 ? index - 1 : -1, x < width - 1 ? index + 1 : -1,
                        y > 0 ? index - width : -1, y < height - 1 ? index + width : -1 };
                    foreach (int next in neighbors) if (next >= 0 && candidate[next] && !visited[next]) {
                        visited[next] = true; queue[tail++] = next;
                    }
                }
                // Preserve enclosed small pale highlights (teeth, flowers).
                if (exterior || tail >= 96) for (int i = 0; i < tail; i++) removed[queue[i]] = true;
            }
            for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
                int index = y * width + x; Color c = pixels[index];
                if (removed[index]) c = Color.Transparent;
                else {
                    int min = Math.Min(c.R, Math.Min(c.G, c.B));
                    int max = Math.Max(c.R, Math.Max(c.G, c.B));
                    bool edge = (x > 0 && removed[index - 1]) || (x < width - 1 && removed[index + 1]) ||
                        (y > 0 && removed[index - width]) || (y < height - 1 && removed[index + width]);
                    if (edge && min >= 100 && max - min <= 30)
                        c = Color.FromArgb(Math.Max(0, Math.Min(255, (150 - min) * 255 / 50)), c.R, c.G, c.B);
                }
                clean.SetPixel(x, y, c);
            }
            if (columns.Length != 6 || rows.Length != 5 || columns[0] != 0 || rows[0] != 0 ||
                columns[5] != width || rows[4] != height) throw new Exception("Invalid 5x4 source regions");
            Array.Clear(visited, 0, count);
            for (int row = 0; row < 4; row++) for (int col = 0; col < 5; col++) {
                for (int sy = rows[row]; sy < rows[row + 1]; sy++) for (int sx = columns[col]; sx < columns[col + 1]; sx++) {
                    int seed = sy * width + sx;
                    if (visited[seed] || clean.GetPixel(sx, sy).A < 24) continue;
                    int head = 0, tail = 1; queue[0] = seed; visited[seed] = true;
                    while (head < tail) {
                        int index = queue[head++], x = index % width, y = index / width;
                        for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
                            int nx = x + dx, ny = y + dy;
                            if (nx < columns[col] || nx >= columns[col+1] || ny < rows[row] || ny >= rows[row+1]) continue;
                            int next = ny * width + nx;
                            if (!visited[next] && clean.GetPixel(nx, ny).A >= 24) { visited[next] = true; queue[tail++] = next; }
                        }
                    }
                    if (tail < 24) for (int i = 0; i < tail; i++) clean.SetPixel(queue[i] % width, queue[i] / width, Color.Transparent);
                }
            }
            var bounds = new Rectangle[20];
            int largest = 0;
            for (int row = 0; row < 4; row++) for (int col = 0; col < 5; col++) {
                int left = columns[col + 1], top = rows[row + 1], right = -1, bottom = -1;
                for (int y = rows[row]; y < rows[row + 1]; y++) for (int x = columns[col]; x < columns[col + 1]; x++) {
                    if (clean.GetPixel(x, y).A < 24) continue;
                    left = Math.Min(left, x); top = Math.Min(top, y); right = Math.Max(right, x); bottom = Math.Max(bottom, y);
                }
                if (right < left || bottom < top) throw new Exception("Empty animation frame");
                if (left <= columns[col] || right >= columns[col + 1] - 1 || top <= rows[row] || bottom >= rows[row + 1] - 1)
                    throw new Exception("Sprite touches a region boundary: " + row + "/" + col);
                bounds[row * 5 + col] = Rectangle.FromLTRB(left, top, right + 1, bottom + 1);
                largest = Math.Max(largest, Math.Max(right - left + 1, bottom - top + 1));
                Console.WriteLine("Frame {0}/{1}: {2},{3} {4}x{5}", row, col, left, top, right-left+1, bottom-top+1);
            }
            using (var atlas = new Bitmap(640, 512, PixelFormat.Format32bppArgb))
            using (var graphics = Graphics.FromImage(atlas)) {
                graphics.Clear(Color.Transparent);
                graphics.InterpolationMode = InterpolationMode.NearestNeighbor;
                graphics.PixelOffsetMode = PixelOffsetMode.Half;
                graphics.CompositingMode = CompositingMode.SourceCopy;
                float scale = 122f / largest;
                for (int row = 0; row < 4; row++) for (int col = 0; col < 5; col++) {
                    Rectangle b = bounds[row * 5 + col];
                    int w = (int)Math.Round(b.Width * scale), h = (int)Math.Round(b.Height * scale);
                    graphics.DrawImage(clean, new Rectangle(col * 128 + (128-w)/2, row * 128 + 125-h, w, h), b, GraphicsUnit.Pixel);
                }
                atlas.Save(output, ImageFormat.Png);
                var hashes = new HashSet<long>();
                for (int row = 0; row < 4; row++) for (int col = 0; col < 5; col++) {
                    int visible = 0, bottom = -1; long hash = 17;
                    for (int y = 0; y < 128; y++) for (int x = 0; x < 128; x++) {
                        Color c = atlas.GetPixel(col*128+x, row*128+y);
                        unchecked { hash = hash * 31 + c.ToArgb(); }
                        if (c.A < 24) continue;
                        visible++; bottom = Math.Max(bottom, y);
                        if (x < 3 || x >= 125 || y < 3 || y >= 125) throw new Exception("Atlas frame clips its padding");
                    }
                    if (visible < 1000 || bottom < 123 || bottom > 124)
                        throw new Exception("Invalid feet pivot or empty atlas frame: " + row + "/" + col + " bottom=" + bottom);
                    if (!hashes.Add(hash)) throw new Exception("Duplicate animation frame");
                }
                Console.WriteLine("PASS: 640x512 RGBA atlas; 20 distinct frames, clear padding, shared feet pivot.");
            }
        }
    }
}
'@
[CharacterSheetImport]::Prepare($InputPath, $OutputPath, $Columns, $Rows)
