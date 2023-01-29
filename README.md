# nebula2-assets-extractor
Extracts and converts assets (packs) for the Nebula Device 2 engine.

Implemented formats (Reading/Writing):
  - NPK0 archives (RW)
  - NTX1 textures (RW)
  - NOB0 scripts (RW)
  - NVX1 models (R)
  - NAX0 animations (R)

## Usage
### GUI
Double-click the JAR file. You need to have Java ≥ 11. You can also start the JAR file in CLI with no arguments.

### CLI

```bash
$ java -jar nebula2-assets-extractor.jar help
Usage: nebula2-extractor [COMMAND]
Commands:
  help             Displays help information about the specified command
  model            Convert an NVX file to a wavefront OBJ
  ntx2img          Convert an NTX1 file to an image
  img2ntx          Convert an image to an NTX1 texture
  unpack           Unpacks an NPK archive
  pack             Packs an NPK archive
  extract          Unpacks an NPK archive and converts all assets
  script           Decompiles a NOB (.n) script
  extract-classes  Extracts a class model to decompile NOB scripts
  wikibox          Generates a Mediawiki Portable Infobox from a script
  gltf             Extracts a mesh, rig, and animation file into a single glTF
                     2.0 file

$ java -jar nebula2-assets-extractor.jar help extract
Usage: nebula2-extractor extract [-d] [-f=<format>] -i=<inputFile>
                                 [-m=<clazzModel>] -o=<outputFolder>
Unpacks an NPK archive and converts all assets
  -d, --delete-old           Deletes original files and unprocessable files
  -f, --format=<format>      texture output image format
  -i, --input=<inputFile>
  -m, --model=<clazzModel>   Json file containing a class model generated using
                               `extract-classes`
  -o, --output=<outputFolder>

$ java -jar nebula2-assets-extractor.jar extract -i data.npk -f png -m project-nomads.classmodel.json -o output -d
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
