package com.phil.potato;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class Potato extends ApplicationAdapter {

	private Random rand = new Random();

	ModelBatch modelBatch;

	Terrain.TerrainChunk chunk;
	Mesh terrainMesh;
	Model terrainModel;
	private float[] heightmap;

	PerspectiveCamera cam;
	CameraInputController camController;
	Environment environment;

	Array<ModelInstance> instancesToRender;
	ModelBuilder terrainMB;
	
	@Override
	public void create () {
		instancesToRender = new Array<ModelInstance>();

		////////////// BUILD TERRAIN MESH /////////////
		chunk = new Terrain.TerrainChunk(31, 31, 4);
		this.heightmap = chunk.heightMap;
		System.out.println(chunk.vertices.length);
		System.out.println(chunk.heightMap.length);
		int len = chunk.vertices.length;
		for (int i = 3; i < len; i += 4) {
			chunk.vertices[i] = Color.toFloatBits(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
			//chunk.vertices[i] = Color.toFloatBits(heightmap[i/4], heightmap[i/4], heightmap[i/4], 255);
		}
		terrainMesh = new Mesh(true, chunk.vertices.length / 3, chunk.indices.length, new VertexAttribute(VertexAttributes.Usage.Position,
				3, "a_position"), new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_color"));

		terrainMesh.setVertices(chunk.vertices);
		terrainMesh.setIndices(chunk.indices);
		///////////////////////////////////////////////


		///////////// BUILD THE TERRAIN MODEL //////////////
		terrainMB = new ModelBuilder();
		terrainMB.begin();
		terrainMB.node().id = "lolmap";
		//terrainMB.part("lolmap", GL20.GL_TRIANGLES, Usage.Position | Usage.ColorPacked, new Material(ColorAttribute.createDiffuse(Color.ORANGE)))
		//		.addMesh(terrainMesh);
		terrainMB.part("lolmap", GL20.GL_TRIANGLES, VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Position, new Material() )
				.addMesh(terrainMesh);
		terrainModel = terrainMB.end();

		ModelInstance terrainInstance = new ModelInstance(terrainModel, "lolmap");
		instancesToRender.add(terrainInstance);
		////////////////////////////////////////////////////


		/////////// INITIALIZE ENVIRONMENT AND CAMERA ////////////
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(3f, 7f, 10f);
		cam.lookAt(0, 4f, 0);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();

		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);
		//////////////////////////////////////////////////////////

		//for rendering
		modelBatch = new ModelBatch();
	}

	@Override
	public void render () {
		final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

		//shift the ground up and down in a sine wave
		//instances.get(0).transform.setTranslation(0f, MathUtils.sinDeg(angle*0.01f) * 2.5f, 20f);

		//dynamicsWorld.stepSimulation(delta, 5, 1f / 60f);

		camController.update();

		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		//| (Gdx.graphics.getBufferFormat().coverageSampling?GL20.GL_COVERAGE_BUFFER_BIT_NV:0));

		modelBatch.begin(cam);
		modelBatch.render(instancesToRender, environment);
		modelBatch.end();


	}
	
	@Override
	public void dispose () {

		modelBatch.dispose();
		terrainMesh.dispose();
		terrainModel.dispose();
	}
}
