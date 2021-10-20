# Voxels

Voxels is a free open-source 3D sandbox game written in Java. The game world is procedurally generated and freely modifiable by the player.

![In-game view](http://i.imgur.com/vlynbDY.jpg "In-game view")

### Features ###

* Varying biomes
* Clouds, caves, trees, sea
* New terrain is continuously generated around the player as they move to new areas.  
Already created areas will stay the same regardless of how far away the player moves from them.
* Terrain modification: the player can remove blocks and add blocks to the world.



https://user-images.githubusercontent.com/59139705/138158865-4460486b-60c6-414d-bddb-6f69297d99b2.mp4



### Technical features ###

* Terrain generation uses both Perlin and Simplex noise algorithms.
* Data compression for faraway terrain to minimize memory usage.
* Greedy meshing to minimize the amount of quads used to draw the terrain.

### Usage ###

Easiest way to try the game is to download the standalone executable jar-file from the runnable folder and run it.

### Contact ###

If you have questions regarding the project, please contact me.

Copyright (C) 2017 Otso Nuortimo  
Released under GNU General Public License.
