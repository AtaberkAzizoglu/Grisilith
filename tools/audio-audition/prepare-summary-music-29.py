"""Forty distinct official CC BY 4.0 Summary candidates, audition only."""
import importlib.util
from pathlib import Path

root=Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('music_audition',root/'prepare-run-music-28.py')
music=importlib.util.module_from_spec(spec)
spec.loader.exec_module(music)
music.OUT=root/'listening-pack-29'
music.PACK_TITLE='Seçki 29 · Summary — Defeat ve Victory'
music.PACK_DESCRIPTION='Defeat için 20, Victory için 20 farklı müzik. Henüz oyuna eklenmedi. Kısa fanfar alternatifleri ayrıca işaretli; uzun parçaları farklı bölümlerinden de dinle.'
music.PACK_LABEL='MUSIC29'
music.GROUPS=[
    ('DF','Defeat · Kayıp ve savaşın ağırlığı',[
        ('Funeral March for Brass','Ağır askerî yas; üflemeliler.',True),
        ('The Pyre','Ortaçağ rengi; kayıp ve ağırlık.',True),
        ('Dark Times','Koyu yaylılar; ağırbaşlı yenilgi.',True),
        ('Loss','Yalın piyano; karanlık kayıp.',True),
        ('Anguish','Arp ve gitar; daha kişisel acı.',False),
        ('At Rest','Dingin vedalaşma; koro ve yaylılar.',False),
        ('Heartbreaking','Kısa, sade piyano hüznü.',False),
        ('Sad Trio','Piyano, çello ve English horn.',False),
        ('Cryptic Sorrow','Uzun ve karanlık piyano.',False),
        ('Night Vigil','Savaşın gölgesi; koro ve çellolar.',False),
        ('Long Road Ahead','Epik ama yaslı; savaş sonrası.',True),
        ('Lamentation','Arpla sade bir ağıt.',False),
        ('Despair and Triumph','Piyano; hüzünle karışık direnç.',False),
        ('Promises to Keep','Hüzünlü ama umudu tükenmemiş.',False),
        ('Virtutes Instrumenti','Eski dünya dokusu; ağırbaşlı.',False),
        ('Virtutes Vocis','İnsan sesi; mistik ve törensel.',False),
        ('Hidden Past','Kelt rengi; yaslı hatırlayış.',False),
        ('Lasting Hope','Koyu atmosferde son umut.',False),
        ('Sovereign Quarter','Sıcak ama karanlık solo piyano.',False),
        ('Final Count','Cenaze yürüyüşünden beslenen dramatik alternatif.',False),
    ]),
    ('VC','Victory · Zafer, coşku ve başarı',[
        ('Fanfare for Space','Güçlü üflemeliler; açık zafer fanfarı.',True),
        ('The Rule','Kısa fanfar; gösterişli zafer.',True),
        ('Dark Star','Kısa fanfar; yükselen orkestral final.',False),
        ('Our Story Begins','Umutlu, görkemli orkestral başarı.',True),
        ('Heroic Age','Aydınlık ve kahramansı hareket.',True),
        ('Call to Adventure','Coşkulu, daha oyuncu orkestral alternatif.',False),
        ('Second Coming - no percussion','Geniş ve ilham veren orkestrasyon.',True),
        ('Americana','Daha büyük, törensel başarı hissi.',False),
        ('Reign','Ağırbaşlı, kazanılmış zafer.',False),
        ('At Launch','Yürüyüş ritmi; ileriye dönük coşku.',False),
        ('Rynos Theme','Yükselip büyüyen kahramanlık.',False),
        ('Egmont Overture Finale','Klasik orkestral, kuvvetli sonuç.',False),
        ('Thaxted (Holst)','Geniş, görkemli ve törensel.',False),
        ('Tempting Secrets','Karanlık dünyaya uygun umut.',False),
        ('Skye Cuillin','Kelt rengi; ferahlayan büyük zafer.',False),
        ('Arcadia','Mistik koro; uhrevî başarı.',False),
        ('Take a Chance','Kısa fanfar; hareketli kutlama.',False),
        ('Evening Melodrama','Kısa fanfar; daha gösterişli alternatif.',False),
        ('Pennsylvania Rose','Daha sıcak, folk dokulu başarı.',False),
        ('Heroic Reception','Çok kısa piyano fanfarı; farklı alternatif.',False),
    ]),
]

if __name__=='__main__':music.main()
