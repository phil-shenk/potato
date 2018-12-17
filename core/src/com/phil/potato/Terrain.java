package com.phil.potato;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector3;

import java.util.Random;

public class Terrain {


    final static class TerrainChunk {
        public final float[] heightMap;
        public final short width;
        public final short height;
        public final float[] vertices;
        public final short[] indices;
        public final int vertexSize;
        private Random rand = new Random();

        public TerrainChunk (int width, int height, int vertexSize) {
            if ((width + 1) * (height + 1) > Short.MAX_VALUE)
                throw new IllegalArgumentException("Chunk size too big, (width + 1)*(height+1) must be <= 32767");

            this.heightMap = new float[(width + 1) * (height + 1)];
            this.width = (short)width;
            this.height = (short)height;
            this.vertices = new float[heightMap.length * vertexSize];
            this.indices = new short[width * height * 6];
            this.vertexSize = vertexSize;
            buildHeightmap("data/heightmap.png");
            buildIndices();
            buildVertices();
            calculateNormals(indices, vertices);
        }

        public void buildHeightmap (String pathToHeightMap) {
            /** get the heightmap from filesystem... should match width and height from current chunk..otherwise its just flat on
             * missing pixel but no error thrown */

            FileHandle handle = Gdx.files.internal(pathToHeightMap);
            Pixmap heightmapImage = new Pixmap(handle);
            Color color = new Color();
            int idh = 0; // index to iterate

            for (int x = 0; x < this.width + 1; x++) {
                for (int y = 0; y < this.height + 1; y++) {
                    // we need seperated channels..
                    Color.rgba8888ToColor(color, heightmapImage.getPixel(x, y)); // better way to get pixel ?
                    // pick whatever channel..we do have a b/w map
                    this.heightMap[idh++] = color.r;

                }
            }
        }

        public void buildVertices () {
            int heightPitch = height + 1;
            int widthPitch = width + 1;

            int idx = 0;
            int hIdx = 0;
            int inc = vertexSize - 6;
            int strength = 4; // multiplier for heightmap

            for (int x = 0; x < widthPitch; x++) {
                for (int z = 0; z < heightPitch; z++) {
                    vertices[idx++] = x;//+rand.nextFloat();
                    vertices[idx++] = heightMap[hIdx++] * strength;
                    vertices[idx++] = z;//+rand.nextFloat();
                    idx++;
                    //vertices[idx++] = rand.nextFloat()*100;
                    //vertices[idx++] = rand.nextFloat()*100;
                    //vertices[idx++] = rand.nextFloat()*100;
                    idx += 3;
                    //idx += inc;
                }
            }
        }

        private void buildIndices () {
            int idx = 0;
            short pitch = (short)(width + 1);
            short i1 = 0;
            short i2 = 1;
            short i3 = (short)(1 + pitch);
            short i4 = pitch;

            short row = 0;

            for (int z = 0; z < height; z++) {
                for (int x = 0; x < width; x++) {
                    indices[idx++] = i1;
                    indices[idx++] = i2;
                    indices[idx++] = i3;

                    indices[idx++] = i3;
                    indices[idx++] = i4;
                    indices[idx++] = i1;

                    i1++;
                    i2++;
                    i3++;
                    i4++;
                }

                row += pitch;
                i1 = row;
                i2 = (short)(row + 1);
                i3 = (short)(i2 + pitch);
                i4 = (short)(row + pitch);
            }
        }

        private void calculateNormals(short[] indices, float[] verts){
            //each square has 2 triangles = 6 indices (w/ 2 pairs of overlap)
            //so i+= 3 iterates over each triangle
            for (int i = 0; i < indices.length; i += 3) {
                int ptAi = indices[i]*vertexSize;
                int ptBi = indices[i+1]*vertexSize;
                int ptCi = indices[i+2]*vertexSize;

                //each pt has 3 pos params
                float x = vertices[ptAi];  //idk if this is the right xyz order
                float y = vertices[ptAi+1];
                float z = vertices[ptAi+2];
                //lol this definitely should not be making vectors to do this im just lazy
                Vector3 ptA = new Vector3(x, y, z);

                x = vertices[ptBi];
                y = vertices[ptBi+1];
                z = vertices[ptBi+2];
                Vector3 ptB = new Vector3(x,y,z);

                x = vertices[ptCi];
                y = vertices[ptCi+1];
                z = vertices[ptCi+2];
                Vector3 ptC = new Vector3(x,y,z);

                Vector3 side1 = ptB.sub(ptA);
                Vector3 side2 = ptC.sub(ptA);
                Vector3 normal = side1.crs(side2);

                float nx = normal.x;
                float ny = normal.y;
                float nz = normal.z;

                //normal x=4, y=5, z=6
                vertices[ptAi+4] = nx;
                vertices[ptBi+4] = nx;
                vertices[ptCi+4] = nx;
                vertices[ptAi+5] = ny;
                vertices[ptBi+5] = ny;
                vertices[ptCi+5] = ny;
                vertices[ptAi+6] = nz;
                vertices[ptBi+6] = nz;
                vertices[ptCi+6] = nz;
            }
        }
    }
}
