# Ampter
Audio Mastering Painter, an app that lets you paint on sound.

---
![image](https://github.com/echometerain/Ampter/assets/70437021/992a6be4-7638-4959-87e5-f33b800d87a9)
***Shortcuts:***
- **Click:** change playhead position
- **Drag:** draw with brush (effect)
- **Scroll:** move left/right one block (.25 second) at a time
- **Shift scroll:** stretch/squeeze spectrograms
- **Left arrow:** move left 1 second (same as “move left” button)
- **Right arrow:** move right 1 second (same as “move right” button)
- **Space:** play/pause
- **(options spinner) Enter:** remove focus from spinner and save changes

Usage:
- Run `pip install -r requirements.txt` to get the python dependencies
- If cloning from github, get the `.jar` file from [Releases](https://github.com/echometerain/Ampter/releases), place it at the root of the repo and click on it (or run `java -jar ./Ampter.jar`)
- If that doesn’t work, open the repo as a netbeans project and manually load the [flatlaf](https://www.formdev.com/flatlaf/) and [jep](https://github.com/ninia/jep) dependencies
  - Build the jar with `Files tab>build.xml>right click>Run Target>Other Targets>package-for-store`

Bugs:
- If any of the three singletons crash the whole program becomes unusable
- Loading a non-audio-file as audio crashes the program
- Loading a non-vst-folder as a vst crashes the program
- If you set the gain too high you get really scary aliasing artefacts
- No spectrograms can be calculated when you are playing audio because jep can only allow python to run in one thread (and I don’t want to pass a multi-megabyte array between two languages)
