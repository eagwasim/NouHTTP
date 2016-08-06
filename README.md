# NouHTTP
NouHTTP is a minimalistic HTTP client for android capable of making post and get requests.
I wanted a simple http client for android and tried [ION] it kept failing on android 4.* < and some samsung devices
so I thought to create a simple HttpUrlConnection wrapper. I hope you find it useful
### Version
0.0.2

### Tech

NouHTTP has only one dependency [GSON] v 2.6 

### Installation

Add the following to your app's build.gradle file

```sh
repositories {
    maven {
        url 'https://dl.bintray.com/eagwasim/maven/'
    }
}
```

Then add the dependency

```sh
 compile 'com.noubug.lib:nouhttp:[version]'
```


### Development

Want to contribute? Great!

### Todos

 - Write Tests
 - Add implementation for PUT,DELETE and UPDATE http methods
 - Add implementation for file upload
 - Add implementation for input stream response

License
----

Apache 2.0


**Free Software!**

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)


   [GSON]: <https://github.com/google/gson/blob/master/README.md>
   [ION]: <https://github.com/koush/ion/blob/master/README.md>
