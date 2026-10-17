$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies System.Drawing.Common,System.Drawing.Primitives,System.Private.Windows.GdiPlus,System.Private.Windows.Core,System.Collections,System.Console -TypeDefinition @'
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
public static class SummonCuts {
    // Flood only the backdrop. Dark, high-contrast pixel-art outlines protect
    // the complete drawing; never slice the sprite at a guessed body rectangle.
    public static Bitmap Cut(Bitmap source, Rectangle cell) {
        int w=cell.Width,h=cell.Height;var barrier=new bool[w*h];var outside=new bool[w*h];
        var pixels=new Color[w*h];for(int y=0;y<h;y++)for(int x=0;x<w;x++)pixels[y*w+x]=source.GetPixel(cell.X+x,cell.Y+y);
        Func<Color,int> light=c=>Math.Max(c.R,Math.Max(c.G,c.B));
        var corners=new Color[]{pixels[0],pixels[w-1],pixels[(h-1)*w],pixels[w*h-1]};
        for(int y=0;y<h;y++)for(int x=0;x<w;x++) {
            float u=x/(float)(w-1),v=y/(float)(h-1);int p=y*w+x;
            float red=corners[0].R*(1-u)*(1-v)+corners[1].R*u*(1-v)+corners[2].R*(1-u)*v+corners[3].R*u*v;
            float g=corners[0].G*(1-u)*(1-v)+corners[1].G*u*(1-v)+corners[2].G*(1-u)*v+corners[3].G*u*v;
            float blue=corners[0].B*(1-u)*(1-v)+corners[1].B*u*(1-v)+corners[2].B*(1-u)*v+corners[3].B*u*v;
            var top=pixels[x];var bottom=pixels[(h-1)*w+x];var left=pixels[y*w];var right=pixels[y*w+w-1];
            red=top.R*(1-v)+bottom.R*v+left.R*(1-u)+right.R*u-red;
            g=top.G*(1-v)+bottom.G*v+left.G*(1-u)+right.G*u-g;
            blue=top.B*(1-v)+bottom.B*v+left.B*(1-u)+right.B*u-blue;
            barrier[p]=Math.Max(Math.Abs(pixels[p].R-red),Math.Max(Math.Abs(pixels[p].G-g),Math.Abs(pixels[p].B-blue)))>20;
        }
        var closed=(bool[])barrier.Clone();
        for(int y=1;y<h-1;y++)for(int x=1;x<w-1;x++)if(barrier[y*w+x])
            for(int yy=-1;yy<=1;yy++)for(int xx=-1;xx<=1;xx++)closed[(y+yy)*w+x+xx]=true;
        var q=new Queue<int>();Action<int> seed=p=>{if(!outside[p]&&!closed[p]){outside[p]=true;q.Enqueue(p);}};
        for(int x=0;x<w;x++){seed(x);seed((h-1)*w+x);}for(int y=0;y<h;y++){seed(y*w);seed(y*w+w-1);}
        while(q.Count>0){int p=q.Dequeue(),x=p%w,y=p/w;if(x>0)seed(p-1);if(x<w-1)seed(p+1);if(y>0)seed(p-w);if(y<h-1)seed(p+w);}
        // Discard detached scraps from neighbouring frames, preserving the main complete body.
        var solid=new bool[w*h];
        for(int y=3;y<h-3;y++)for(int x=3;x<w-3;x++) {
            int p=y*w+x;solid[p]=!outside[p];
            for(int yy=-3;yy<=3;yy++)for(int xx=-3;xx<=3;xx++)if(outside[p+yy*w+xx])solid[p]=false;
        }
        var seen=new bool[w*h];var best=new List<int>();
        for(int p=0;p<w*h;p++)if(solid[p]&&!seen[p]) {
            var group=new List<int>();q.Enqueue(p);seen[p]=true;
            while(q.Count>0){int n=q.Dequeue();group.Add(n);int x=n%w,y=n/w;
                int[] ns={x>0?n-1:-1,x<w-1?n+1:-1,y>0?n-w:-1,y<h-1?n+w:-1};
                foreach(int a in ns)if(a>=0&&solid[a]&&!seen[a]){seen[a]=true;q.Enqueue(a);}
            }
            if(group.Count>best.Count)best=group;
        }
        var restored=new HashSet<int>(best);
        foreach(int p in best)for(int yy=-3;yy<=3;yy++)for(int xx=-3;xx<=3;xx++) {
            int x=p%w+xx,y=p/w+yy;if(x>=0&&x<w&&y>=0&&y<h&&!outside[y*w+x])restored.Add(y*w+x);
        }
        best=new List<int>(restored);
        // Dilation can leave detached pieces at neighbouring frame edges.
        // Recheck connectivity after reconstruction, not just before it.
        var finalSeen=new HashSet<int>();var finalBody=new List<int>();
        foreach(int start in restored)if(!finalSeen.Contains(start)) {
            var group=new List<int>();q.Enqueue(start);finalSeen.Add(start);
            while(q.Count>0){int p=q.Dequeue();group.Add(p);
                for(int yy=-1;yy<=1;yy++)for(int xx=-1;xx<=1;xx++) {
                    int x=p%w+xx,y=p/w+yy,n=y*w+x;
                    if(x>=0&&x<w&&y>=0&&y<h&&restored.Contains(n)&&finalSeen.Add(n))q.Enqueue(n);
                }
            }
            if(group.Count>finalBody.Count)finalBody=group;
        }
        best=finalBody;
        if(best.Count<500)throw new Exception("Outline extraction failed "+cell+" pixels="+best.Count);
        int l=w,t=h,r=0,b=0;foreach(int p in best){l=Math.Min(l,p%w);t=Math.Min(t,p/w);r=Math.Max(r,p%w);b=Math.Max(b,p/w);}
        var result=new Bitmap(r-l+1,b-t+1,PixelFormat.Format32bppArgb);
        foreach(int p in best)result.SetPixel(p%w-l,p/w-t,pixels[p]);
        Console.WriteLine("CUT "+cell+" content="+l+","+t+","+result.Width+","+result.Height+" pixels="+best.Count);
        return result;
    }
    public static void Pack(string input,string output,string name,int[] xs,int[] ys,int[] rows,string[] directions,int count) {
        using(var source=new Bitmap(input)) {
            var sprites=new List<Bitmap>();int maxW=0,maxH=0;
            foreach(int row in rows)for(int col=0;col<count;col++) {
                int left=Math.Max(0,xs[col]-12),top=Math.Max(0,ys[row]-2);
                int right=Math.Min(source.Width,xs[col+1]+12),bottom=Math.Min(source.Height,ys[row+1]+2);
                if(name=="toad")right=xs[col+1]-24;
                if(name=="knight") {
                    int[][] lefts={new[]{565,751,944,1137,1327},new[]{589,777,969,1152,1340},new[]{587,750,948,1133,1334},new[]{560,744,936,1120,1310}};
                    int[][] rights={new[]{720,915,1095,1296,1480},new[]{744,932,1129,1316,1510},new[]{749,937,1130,1308,1512},new[]{720,928,1116,1308,1512}};
                    int[] tops={0,176,338,483},bottoms={173,333,482,630};
                    left=lefts[row][col];right=rights[row][col];top=tops[row];bottom=bottoms[row];
                }
                if(name=="imp"||name=="cow") { top=ys[row];bottom=ys[row+1]-3; }
                if(name=="imp") { left=xs[col];right=xs[col+1]; }
                if(name=="guard"&&row==2)bottom=768;
                var sprite=Cut(source,new Rectangle(left,top,right-left,bottom-top));
                sprites.Add(sprite);maxW=Math.Max(maxW,sprite.Width);maxH=Math.Max(maxH,sprite.Height);
            }
            // Same native pixel scale and pivot in every frame, with safe transparent margins.
            int side=Math.Max(maxW,maxH)+16;
            using(var preview=new Bitmap(side*count,side*rows.Length,PixelFormat.Format32bppArgb))
            using(var pg=Graphics.FromImage(preview)) {
                pg.Clear(Color.FromArgb(255,64,90,66));
                for(int i=0;i<sprites.Count;i++)using(var frame=new Bitmap(side,side,PixelFormat.Format32bppArgb)) {
                    using(var g=Graphics.FromImage(frame))g.DrawImageUnscaled(sprites[i],(side-sprites[i].Width)/2,side-8-sprites[i].Height);
                    string path=System.IO.Path.Combine(output,name+"_"+directions[i/count]+"_"+(i%count)+".png");
                    frame.Save(path,ImageFormat.Png);pg.DrawImageUnscaled(frame,(i%count)*side,(i/count)*side);
                }
                preview.Save(System.IO.Path.Combine(output,"preview.png"),ImageFormat.Png);
            }
            foreach(var s in sprites)s.Dispose();Console.WriteLine("PACK "+name+" frames="+sprites.Count+" side="+side);
        }
    }
}
'@
$taskSource='C:/Users/aaata/OneDrive/Masaüstü/Görseller/Sheets/summons'
$taskOutput=Join-Path $PSScriptRoot 'summon-cuts'
New-Item -ItemType Directory -Force -Path $taskOutput | Out-Null
function Extract($file,$name,$xs,$ys,$rows,$directions,$count) {
    $dest=Join-Path $taskOutput $name
    New-Item -ItemType Directory -Force -Path $dest | Out-Null
    [SummonCuts]::Pack((Join-Path $taskSource $file),$dest,$name,$xs,$ys,$rows,$directions,$count)
}
Extract 'toad sprite sheet.png' 'toad' @(0,384,768,1152,1536) @(0,270,492,721,1024) @(0,2,3,1) @('south','east','north','west') 4
Extract 'c1122b38-a46a-42a6-8d8f-0fb6a6bd690b.png' 'knight' @(565,751,944,1137,1327,1536) @(0,176,336,486,635,768,884,1024) @(0,2,1,3) @('south','east','north','west') 5
Extract '9ad3517c-c2f8-4f18-a74a-dc56f0e18164.png' 'cow' @(530,760,991,1214,1440) @(0,143,285,398,512,637,747,862,1024) @(0,3,1,2) @('south','east','north','west') 4
Extract 'b8096031-2bac-48d5-b4ac-186cafa45f44.png' 'imp' @(710,924,1139,1339,1536) @(0,144,278,392,512,642,761,885,1024) @(0,3,1,2) @('south','east','north','west') 4
Extract 'guard sprite sheet.png' 'guard' @(423,628,830,1032,1234,1440) @(0,277,525,772,1024) @(0,1,2,3) @('south','east','north','west') 5
