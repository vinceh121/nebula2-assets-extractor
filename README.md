# nebula2-assets-extractor
Extracts and converts assets (packs) for the Nebula Device 2 engine.

Implemented formats (Reading/Writing):
  - NPK0 archives (RW)
  - NTX1 textures (R)
  - NVX1 models (R)
  - NOB0 scripts (R)

## Usage

```bash
$ java -jar nebula2-assets-extractor.jar
Usage: nebula2-extractor [COMMAND]
Commands:
  help             Displays help information about the specified command
  model            Convert an NVX file to a wavefront OBJ
  texture          Convert an NTX file to an image
  unpack           Unpacks an NPK archive
  extract          Unpacks an NPK archive and converts all assets
  script           Decompiles a NOB (.n) script
  extract-classes  Extracts a class model to decompile NOB scripts

$ java -jar nebula2-assets-extractor.jar help extract
Usage: nebula2-extractor extract [-d] [-f=<format>] -i=<inputFile>
                                 -o=<outputFolder>
Unpacks an NPK archive and converts all assets
  -d, --delete-old          Deletes original files and unprocessable files
  -f, --format=<format>     texture output image format
  -i, --input=<inputFile>
  -o, --output=<outputFolder>

$ java -jar nebula2-assets-extractor.jar extract -i data.npk -o output -d
$ tree output
output
├── a_ammo.n
│   ├── collide.obj
│   ├── debris01.obj
│   ├── debris02.obj
│   ├── debris03.obj
│   ├── debris04.obj
│   ├── model.obj
│   └── texturenone.png
├── a_arte_pplant.n
│   ├── a_arte_pplant.obj
│   ├── collide.obj
│   └── texturealpha.png
├── a_barrel01.n
│   ├── collide.obj
│   ├── model.obj
│   ├── plane01.obj
│   └── texturenone.png
```

## License

This project is licensed under the GNU General Public License V3. For a copy, please see the LICENSE file.
