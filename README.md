# marissa-java [![Travis](https://img.shields.io/travis/ninjabear/marissa-java.svg)]() [![GitHub release](https://img.shields.io/github/release/ninjabear/marissa-java.svg)]() [![GitHub license](https://img.shields.io/github/license/ninjabear/marissa-java.svg)]()

A Java8 hipchat bot

# Features

|Command                | Result                                                   |
|-----------------------|----------------------------------------------------------|
|marissa time           | Returns the time                                         |
|marissa echo <text>    | Echo given <text>                                        |
|marissa animate <text> | Find an animated gif for <text>                          |
|marissa image <text>   | Find an image for <text>                                 |
|marissa search <text>  | Search the web and return the best result for <text>     |
|marissa selfie         | Sends you a selfie                                       |
|marissa score          | Returns the current score marissa has. You can reward marissa with marissa +1 (or marissa +100), or punish marissa with marissa -1 (or -100..) |


# Set up

Marissa requires the following things for everything to work;

| Needs           | Because                                    |
|-----------------|--------------------------------------------|
| A hipchat login | In order to be in your room                |
| A bing API key  | To perform web searches/images searches and animated image searches |
| A giphy API key | To perform animated image searches. ([A public beta key is available here.](https://github.com/Giphy/GiphyAPI#public-beta-key)) |

This config is loaded from a file in the same directory as the jar, which must be called `persist.json`. It looks like this;

```{json}
{
  "core":{
    "userid":"***@chat.hipchat.com",
    "password":"***",
    "nickname":"Mars",
    "joinroom":"***"
  },
  "bingsearch":{
    "appid":"***"
  },
  "giphysearch":{
    "apikey":"***"
  }
}
```

with `***` replaced with your values.

# Running

Marissa is deployed as a capsuled jar - you only need java8 and to execute:

`java -jar marissa-capsule.jar`

providing you've set up your `persist.json`
