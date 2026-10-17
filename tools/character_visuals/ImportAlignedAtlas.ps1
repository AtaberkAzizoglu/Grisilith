param(
    [Parameter(Mandatory=$true)][string]$InputPath,
    [Parameter(Mandatory=$true)][string]$OutputPath,
    [Parameter(Mandatory=$true)][int[]]$Columns,
    [Parameter(Mandatory=$true)][int[]]$Rows,
    [int]$CellSize=128,
    [switch]$ConnectedSprites
)
# Mechanical atlas extraction, not artwork generation: retain RGBA drawings,
# remove only detached alpha specks, and pack full bodies onto a common feet pivot.
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies System.Drawing.Common,System.Drawing.Primitives,System.Private.Windows.GdiPlus,System.Private.Windows.Core,System.Collections,System.Console -TypeDefinition @'
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.Drawing.Drawing2D;
public static class AlignedAtlas {
    public static void Validate(string path,int cell) {
        using(var b=new Bitmap(path)) {
            if(b.Width%cell!=0||b.Height%cell!=0)throw new Exception("Invalid atlas dimensions");
            int frames=0;
            for(int row=0;row<b.Height/cell;row++)for(int col=0;col<b.Width/cell;col++) {
                int count=0;
                for(int y=0;y<cell;y++)for(int x=0;x<cell;x++)if(b.GetPixel(col*cell+x,row*cell+y).A>=24) {
                    count++;
                    if(x<4||x>=cell-4||y<4||y>=cell-4)throw new Exception("Clipped frame "+path+" "+row+","+col);
                }
                if(count<100)throw new Exception("Empty sprite");
                frames++;
            }
            Console.WriteLine("PADDING_PASS "+path+" frames="+frames);
        }
    }
    public static void Connected(string input,string output,int cols,int rows,int cell,int minY,int minX,int maxX) {
        using(var source=new Bitmap(input)) {
            int w=source.Width,h=source.Height;var seen=new bool[w*h];var queue=new int[w*h];
            var groups=new List<int>[cols*rows];for(int i=0;i<groups.Length;i++)groups[i]=new List<int>();
            // Complete connected drawings, never sliced at an assumed grid boundary.
            for(int y=minY;y<h;y++)for(int x=minX;x<maxX;x++) {
                int seed=y*w+x;if(seen[seed]||source.GetPixel(x,y).A<24)continue;
                int head=0,tail=1;queue[0]=seed;seen[seed]=true;long sumX=0,sumY=0;
                while(head<tail) {
                    int p=queue[head++],px=p%w,py=p/w;sumX+=px;sumY+=py;
                    int[] ns={px>minX?p-1:-1,px<maxX-1?p+1:-1,py>minY?p-w:-1,py<h-1?p+w:-1};
                    foreach(int n in ns)if(n>=0&&!seen[n]&&source.GetPixel(n%w,n/w).A>=24) {
                        seen[n]=true;queue[tail++]=n;
                    }
                }
                if(tail<8)continue;
                int col=Math.Min(cols-1,Math.Max(0,(int)(((float)sumX/tail-minX)*cols/(maxX-minX))));
                int row=Math.Min(rows-1,Math.Max(0,(int)(((float)sumY/tail-minY)*rows/(h-minY))));
                for(int j=0;j<tail;j++)groups[row*cols+col].Add(queue[j]);
            }
            var sprites=new List<Bitmap>();int widest=1,tallest=1;
            foreach(var group in groups) {
                if(group.Count<100)throw new Exception("Missing connected sprite");
                int l=w,t=h,r=0,b=0;foreach(int p in group){l=Math.Min(l,p%w);r=Math.Max(r,p%w);t=Math.Min(t,p/w);b=Math.Max(b,p/w);}
                var sprite=new Bitmap(r-l+1,b-t+1,PixelFormat.Format32bppArgb);
                foreach(int p in group)sprite.SetPixel(p%w-l,p/w-t,source.GetPixel(p%w,p/w));
                sprites.Add(sprite);widest=Math.Max(widest,sprite.Width);tallest=Math.Max(tallest,sprite.Height);
                Console.WriteLine("CONNECTED "+sprites.Count+" "+l+","+t+","+(r-l+1)+","+(b-t+1));
            }
            float scale=Math.Min((cell-16f)/widest,(cell-12f)/tallest);
            using(var atlas=new Bitmap(cols*cell,rows*cell,PixelFormat.Format32bppArgb))
            using(var g=Graphics.FromImage(atlas)) {
                g.CompositingMode=CompositingMode.SourceCopy;g.InterpolationMode=InterpolationMode.NearestNeighbor;g.PixelOffsetMode=PixelOffsetMode.Half;
                for(int i=0;i<sprites.Count;i++) {
                    int dw=(int)Math.Round(sprites[i].Width*scale),dh=(int)Math.Round(sprites[i].Height*scale);
                    g.DrawImage(sprites[i],new Rectangle(i%cols*cell+(cell-dw)/2,i/cols*cell+cell-6-dh,dw,dh),
                        0,0,sprites[i].Width,sprites[i].Height,GraphicsUnit.Pixel);
                }
                atlas.Save(output,ImageFormat.Png);
            }
            foreach(var sprite in sprites)sprite.Dispose();
            Console.WriteLine("CONNECTED_ATLAS_PASS "+output+" frames="+groups.Length);
        }
    }
    public static void Import(string input,string output,int[] xs,int[] ys,int cell) {
        using(var source=new Bitmap(input)) {
            int cols=xs.Length-1,rows=ys.Length-1;
            var bounds=new List<Rectangle>();
            var sprites=new List<Bitmap>();
            int widest=1,tallest=1;
            for(int row=0;row<rows;row++) for(int col=0;col<cols;col++) {
                int w=xs[col+1]-xs[col],h=ys[row+1]-ys[row];
                var seen=new bool[w*h];var keep=new bool[w*h];var queue=new int[w*h];
                for(int sy=0;sy<h;sy++) for(int sx=0;sx<w;sx++) {
                    int seed=sy*w+sx;
                    if(seen[seed] || source.GetPixel(xs[col]+sx,ys[row]+sy).A<24) continue;
                    int head=0,tail=1;queue[0]=seed;seen[seed]=true;
                    while(head<tail) {
                        int i=queue[head++],x=i%w,y=i/w;
                        int[] ns={x>0?i-1:-1,x<w-1?i+1:-1,y>0?i-w:-1,y<h-1?i+w:-1};
                        foreach(int n in ns) if(n>=0&&!seen[n]&&source.GetPixel(xs[col]+n%w,ys[row]+n/w).A>=24) {
                            seen[n]=true;queue[tail++]=n;
                        }
                    }
                    if(tail>=8) for(int j=0;j<tail;j++)keep[queue[j]]=true;
                }
                int left=w,top=h,right=-1,bottom=-1;
                for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(keep[y*w+x]) {
                    left=Math.Min(left,x);top=Math.Min(top,y);right=Math.Max(right,x);bottom=Math.Max(bottom,y);
                }
                if(right<left) throw new Exception("Empty frame "+row+","+col);
                if(left==0||top==0||right==w-1||bottom==h-1)
                    Console.WriteLine("EDGE_WARNING "+row+","+col+" "+left+","+top+","+right+","+bottom);
                var b=Rectangle.FromLTRB(left,top,right+1,bottom+1);
                var sprite=new Bitmap(b.Width,b.Height,PixelFormat.Format32bppArgb);
                for(int y=0;y<b.Height;y++)for(int x=0;x<b.Width;x++) {
                    // Preserve faint edge alpha next to retained body pixels.
                    int px=x+left,py=y+top;bool body=false;
                    for(int yy=Math.Max(0,py-1);yy<=Math.Min(h-1,py+1);yy++)
                        for(int xx=Math.Max(0,px-1);xx<=Math.Min(w-1,px+1);xx++)body|=keep[yy*w+xx];
                    if(body)sprite.SetPixel(x,y,source.GetPixel(xs[col]+px,ys[row]+py));
                }
                bounds.Add(b);sprites.Add(sprite);widest=Math.Max(widest,b.Width);tallest=Math.Max(tallest,b.Height);
            }
            // One scale for the whole sheet prevents size pumping during walking.
            float scale=Math.Min((cell-16f)/widest,(cell-12f)/tallest);
            using(var atlas=new Bitmap(cols*cell,rows*cell,PixelFormat.Format32bppArgb))
            using(var g=Graphics.FromImage(atlas)) {
                g.CompositingMode=CompositingMode.SourceCopy;
                g.InterpolationMode=InterpolationMode.NearestNeighbor;
                g.PixelOffsetMode=PixelOffsetMode.Half;
                for(int i=0;i<sprites.Count;i++) {
                    var b=bounds[i];int w=(int)Math.Round(b.Width*scale),h=(int)Math.Round(b.Height*scale);
                    int x=(i%cols)*cell+(cell-w)/2,y=(i/cols)*cell+cell-6-h;
                    g.DrawImage(sprites[i],new Rectangle(x,y,w,h),0,0,b.Width,b.Height,GraphicsUnit.Pixel);
                    Console.WriteLine("FRAME "+i+" source="+b+" packed="+x+","+y+","+w+","+h);
                }
                atlas.Save(output,ImageFormat.Png);
            }
            foreach(var sprite in sprites)sprite.Dispose();
            Console.WriteLine("ATLAS_PASS "+output+" frames="+sprites.Count+" scale="+scale);
        }
    }
}
'@
if($ConnectedSprites) {
    [AlignedAtlas]::Connected((Resolve-Path -LiteralPath $InputPath).Path,[IO.Path]::GetFullPath($OutputPath),
        $Columns.Length-1,$Rows.Length-1,$CellSize,$Rows[0],$Columns[0],$Columns[-1])
} else {
    [AlignedAtlas]::Import((Resolve-Path -LiteralPath $InputPath).Path,[IO.Path]::GetFullPath($OutputPath),$Columns,$Rows,$CellSize)
}
[AlignedAtlas]::Validate([IO.Path]::GetFullPath($OutputPath),$CellSize)
