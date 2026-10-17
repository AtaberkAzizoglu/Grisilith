param([string]$Root='C:\Android Projects\Grisilith')
Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies System.Drawing.Common,System.Drawing.Primitives,System.Private.Windows.GdiPlus,System.Private.Windows.Core,System.Collections -TypeDefinition @'
using System; using System.Drawing; using System.Collections.Generic;
public static class SpriteBoundsAudit {
 public static string Bounds(string path,int minX,int columns,int rows) {
  using(var b=new Bitmap(path)) {
   int w=b.Width,h=b.Height; var seen=new bool[w*h]; var q=new int[w*h];
   var rects=new Rectangle[columns*rows];
   for(int y=0;y<h;y++)for(int x=minX;x<w;x++) {
    int seed=y*w+x; if(seen[seed]||b.GetPixel(x,y).A<24)continue;
    int head=0,tail=1,l=x,r=x,t=y,bt=y;long sx=0,sy=0;q[0]=seed;seen[seed]=true;
    while(head<tail) {int p=q[head++],px=p%w,py=p/w;sx+=px;sy+=py;l=Math.Min(l,px);r=Math.Max(r,px);t=Math.Min(t,py);bt=Math.Max(bt,py);
     int[] ns={px>minX?p-1:-1,px<w-1?p+1:-1,py>0?p-w:-1,py<h-1?p+w:-1};
     foreach(int n in ns)if(n>=0&&!seen[n]&&b.GetPixel(n%w,n/w).A>=24){seen[n]=true;q[tail++]=n;}
    }
    if(tail<8)continue;
    int col=Math.Min(columns-1,Math.Max(0,(int)(((double)sx/tail-minX)*columns/(w-minX))));
    int row=Math.Min(rows-1,Math.Max(0,(int)((double)sy/tail*rows/h)));
    int i=row*columns+col;var rect=new Rectangle(l,t,r-l+1,bt-t+1);
    rects[i]=rects[i].IsEmpty?rect:Rectangle.Union(rects[i],rect);
   }
   var strings=new List<string>();foreach(var rect in rects)strings.Add("EnemySpriteFrame("+rect.X+", "+rect.Y+", "+rect.Width+", "+rect.Height+")");
   return string.Join(",\n",strings);
  }
 }
}
'@
$cases=@(
 @{n='bear';p='beast/grizzly_bear.png';x=185;r=4},
 @{n='crocodile';p='beast/crocodile.png';x=0;r=4},
 @{n='spearman_front';p='humanoid/spearman_front.png';x=0;r=1},
 @{n='spearman_left';p='humanoid/spearman_left.png';x=0;r=1},
 @{n='spearman_back';p='humanoid/spearman_back.png';x=0;r=1}
)
foreach($case in $cases) {
 Write-Output $case.n
 Write-Output ([SpriteBoundsAudit]::Bounds((Join-Path $Root ('assets/enemies/'+$case.p)),$case.x,5,$case.r))
}
