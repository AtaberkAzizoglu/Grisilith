"""Reviewed spoken-label intervals, seconds in decoded original MP3 audio.
These are exclusion intervals, NOT reaction boundaries. All sound until the next
label is retained, including multi-part cries/laughs and quiet endings.
The original review grid used 220-sample frames labelled as nominal 5ms.
Convert that review grid to exact native timestamps before consuming these data.
"""
SOURCES=[
 dict(file='imbert hasar.mp3',character='IM',name='Sir Imbert',selection=(0.,5.22),groups=[
  dict(kind='H',end=26.4359,labels=[(5.655,6.33),(7.675,8.43),(9.75,10.47),(11.87,12.615),(14.19,14.985),(16.17,16.87),(18.77,19.435),(20.74,21.395),(22.935,23.685),(24.935,25.415)])]),
 dict(file='imbert death.mp3',character='IM',name='Sir Imbert',groups=[
  dict(kind='D',end=26.2008,labels=[(0.,1.13),(2.775,3.81),(5.415,6.44),(8.06,9.125),(10.625,11.72),(14.08,15.045),(16.555,17.59),(19.21,20.14),(21.68,22.64),(24.1,24.645)])]),
 dict(file='Ywaleth Voices.mp3',character='YW',name='Ywaleth Yoster',selection=(0.,3.65),groups=[
  dict(kind='H',end=24.16,labels=[(4.44,5.88),(7.165,7.805),(9.17,9.86),(11.01,11.72),(12.905,13.805),(14.885,15.69),(16.895,17.63),(18.83,19.51),(20.69,21.395),(22.595,23.015)]),
  dict(kind='D',end=45.208,labels=[(24.285,25.51),(26.99,27.645),(29.13,29.795),(31.24,31.955),(33.24,34.115),(35.285,36.1),(37.415,38.165),(39.5,40.19),(41.64,42.36),(43.64,44.065)])]),
 dict(file='linlos voices.mp3',character='LN',name='Linlos Dustman',selection=(0.,7.65),groups=[
  dict(kind='H',end=25.6,labels=[(8.47,9.085),(10.055,10.705),(11.83,12.54),(13.54,14.29),(15.155,15.86),(16.95,17.74),(18.605,19.35),(20.485,21.19),(22.24,22.955),(23.775,24.22)]),
  dict(kind='D',end=47.538,labels=[(26.28,26.9),(28.555,29.21),(30.715,31.405),(33.025,33.75),(35.205,35.91),(37.6,38.375),(39.935,40.68),(41.925,42.61),(44.07,44.775),(45.825,46.275)])]),
 dict(file='Koba Voices.mp3',character='KB',name='Koba',selection=(0.,8.2),groups=[
  dict(kind='H',end=33.18,labels=[(9.2,10.045),(11.45,12.375),(13.595,14.505),(16.075,16.96),(19.44,20.415),(22.01,22.965),(24.205,25.135),(26.5,27.3),(28.985,29.93),(31.205,31.775)]),
  dict(kind='D',end=64.53,labels=[(33.29,34.74),(36.955,37.81),(39.945,40.835),(42.345,43.24),(45.31,46.3),(48.325,49.27),(51.305,52.24),(54.27,55.08),(58.95,59.915),(61.96,62.53)])]),
 dict(file='Lavena Vol Voices.mp3',character='LV',name='Lavena Vol',selection=(0.,6.4),groups=[
  dict(kind='H',end=26.8,labels=[(7.37,8.08),(9.41,10.135),(11.455,12.205),(13.38,14.135),(15.22,16.08),(17.275,18.135),(19.28,20.07),(21.285,22.02),(23.15,23.925),(25.125,25.57)]),
  dict(kind='D',end=48.638,labels=[(27.615,28.31),(29.78,30.5),(32.09,32.82),(34.2,34.94),(36.275,36.99),(38.36,39.215),(40.505,41.3),(42.7,43.43),(44.715,45.49),(46.745,47.21)])]),
 dict(file='Witch Voices.mp3',character='WI',name='Witch',groups=[
  dict(kind='H',end=18.02,labels=[(1.,1.45),(2.49,3.1),(4.18,4.7),(5.745,6.285),(7.41,8.035),(9.165,9.805),(10.845,11.455),(12.695,13.185),(14.21,14.77),(16.06,16.53)]),
  dict(kind='D',end=38.7,labels=[(18.965,19.4),(20.95,21.39),(22.745,23.3),(24.685,25.2),(26.415,27.005),(29.205,29.87),(31.17,31.795),(33.21,33.69),(34.835,35.395),(36.7,37.185)]),
  dict(kind='P',end=71.914,missing_labels=[2],labels=[(39.795,40.22),(43.19,43.19),(45.355,45.905),(48.32,48.835),(52.24,52.77),(55.475,56.105),(58.71,59.295),(61.045,61.82),(64.66,65.215),(67.575,68.035)])]),
 dict(file='Vampire Lord Voices.mp3',character='VL',name='Vampire Lord',groups=[
  dict(kind='H',end=21.75,labels=[(1.02,1.55),(2.575,3.13),(4.515,5.04),(6.175,6.765),(8.18,8.695),(10.52,11.695),(13.255,13.84),(15.285,15.755),(17.395,17.9),(19.345,19.79)]),
  dict(kind='D',end=49.8,labels=[(22.86,23.335),(26.14,26.59),(28.33,28.81),(31.105,31.635),(32.835,33.345),(35.32,36.005),(37.625,38.21),(41.82,42.295),(44.615,45.11),(47.285,47.7)]),
  dict(kind='P',end=78.328,labels=[(50.92,51.365),(53.96,54.415),(56.935,57.36),(59.645,60.095),(62.955,63.365),(66.035,66.685),(68.47,69.01),(71.47,71.88),(73.015,73.52),(75.335,75.755)])]),
]

ENERGY_TIME_SCALE=220/(44100*.005)
for _source in SOURCES:
    if 'selection' in _source:
        _source['selection']=tuple(t*ENERGY_TIME_SCALE for t in _source['selection'])
    for _group in _source['groups']:
        _group['end']*=ENERGY_TIME_SCALE
        _group['labels']=[tuple(t*ENERGY_TIME_SCALE for t in interval) for interval in _group['labels']]
