# Line Webtoon Duplicator

<div align="center">
    <img src="https://raw.githubusercontent.com/chanlugeon/line-webtoon-duplicator/master/logo.png"><br><br>
</div>

All content in Line Webtoon can be downloaded **any number of times** and
stored **forever** due to *Line Webtoon Duplicator*.  

## Download
[line-webtoon-duplicator.exe][exe]  
[line-webtoon-duplicator.jar][jar]

## Generated files
### Webtoon folder
`thumbnail.jpg`: Thumbnail of the webtoon.   
`webtoon-info.json`: Information about the webtoon; the keys are:

* _artists_: Artists of the webtoon.
* _codeName_: Title that is used only small letters and hyphen instead of
              whitespace. e.g. _tower-of-god_.
* _dayOfWeek_: Day of week that is updated the webtoon. if the webtoon is completed,
               the value is _end_.
* _genre_: Genre of the webtoon.
* _rating_: Rating of the webtoon.
* _summary_: Summary of the webtoon.
* _title_: Name of the webtoon.
* _titleNo_: Unique number of the webtoon.

#### Episode folder
`(number).jpg`: Content of the episode. Read in numerical oreder from smallest to
             largest.  
`thumbnail.jpg`: Thumbnail of the episode.  
`episode-info.json`: Information about the episode; the keys are:

* _episodeNo_: Unique number of the episode.
* _likes_: Like count of the episode.
* _title_: Name of the episode.

## What's new
*  **22/01/2019:** Line Webtoon Duplicator launched.

## Used libraries
+ Gson
+ Jsoup



[exe]: https://raw.githubusercontent.com/chanlugeon/line-webtoon-duplicator/master/line-webtoon-duplicator.exe
[jar]: https://raw.githubusercontent.com/chanlugeon/line-webtoon-duplicator/master/line-webtoon-duplicator.jar
