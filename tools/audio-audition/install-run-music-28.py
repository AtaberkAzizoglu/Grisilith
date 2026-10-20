"""Install only the user's ten approved, unchanged CC BY 4.0 music files."""
import hashlib
import json
from pathlib import Path
import shutil

ROOT=Path(__file__).resolve().parent
PROJECT=ROOT.parent.parent
SOURCE=ROOT/'listening-pack-28'
DEST=PROJECT/'assets/audio/music'
SELECTED={
    'CF2':'forest_normal.mp3','CF5':'forest_mini.mp3','VS9':'forest_final.mp3',
    'VS1':'siege_normal.mp3','VS2':'siege_mini.mp3','VS6':'siege_final.mp3',
    'HD1':'depths_normal.mp3','HD8':'depths_mini.mp3','HD10':'depths_final.mp3',
    'MK2':'market.mp3',
}

def main():
    rows={r['code']:r for r in json.loads((SOURCE/'MUSIC-SOURCES.json').read_text(encoding='utf-8'))}
    DEST.mkdir(parents=True,exist_ok=True)
    manifest=[]
    credits=['Grisilith — Run, boss-stage and market music (user-approved Seçki 28).',
        'All recordings: Kevin MacLeod (incompetech.com).',
        'Licensed under Creative Commons: By Attribution 4.0 License',
        'https://creativecommons.org/licenses/by/4.0/',
        'No edits to the recordings. Playback loops at the end of each complete track.',
        'Menu music is credited separately in CREDITS.txt.', '']
    for code,target in SELECTED.items():
        row=rows[code]
        original=SOURCE/row['file']
        assert hashlib.sha256(original.read_bytes()).hexdigest()==row['sha256']
        shutil.copyfile(original,DEST/target)
        manifest.append(dict(row,installed=target))
        credits += [f'{code}: "{row["title"]}" Kevin MacLeod (incompetech.com)',
            row['source'],row['license'],'File: '+target,'SHA256: '+row['sha256'],'']
    (DEST.parent/'RUN-MUSIC-SELECTIONS.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
    (DEST.parent/'RUN-MUSIC-LICENSES.txt').write_text('\n'.join(credits),encoding='utf-8')
    print('RUN_MUSIC_INSTALLED: 10 exact approved full tracks; attribution and hashes saved.')

if __name__=='__main__':main()
