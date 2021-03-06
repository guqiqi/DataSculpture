

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLSLProgram;
import com.thomasdiewald.pixelflow.java.fluid.DwFluid2D;
import com.thomasdiewald.pixelflow.java.fluid.DwFluidStreamLines2D;

import processing.core.*;
import processing.opengl.PGraphics2D;

import java.io.FileReader;

int viewport_w = 1280;
int viewport_h = 720;
int viewport_x = 230;
int viewport_y = 0;

int fluidgrid_scale = 1;

// library
DwPixelFlow context;

// Fluid simulation
DwFluid2D fluid;

// streamline visualization
DwFluidStreamLines2D streamlines;

// render targets
PGraphics2D pg_fluid;
//texture-buffer, for adding obstacles
PGraphics2D pg_obstacles;
// custom line render
DwGLSLProgram customstreamlinerenderer;

// data parameter
ArrayList<EEG> mindData;
EEG data = new EEG();
int dataIndex = 0;

// some state variables for the GUI/display
int     BACKGROUND_COLOR           = 39;
int     STREAMLINE_DENSITY         = 10;

// image control
FluidCircle[] fluidCircles = new FluidCircle[2];

int count = -1;
int countCycle = 60;

private class MyFluidData implements DwFluid2D.FluidData {
  // update() is called during the fluid-simulation update step.
  @Override
    public void update(DwFluid2D fluid) {

    float px, py, vx, vy, vscale;

    if (frameCount % 5 == 0) {
      //yi duan yuan hu, change direction after one round
      for (int i = 0; i < 2; i++) {
        if (fluidCircles[i].angleNum == 0) {
          fluidCircles[i].direction = fluidCircles[i].direction * -1.0;

          fluidCircles[i].ppx1 = fluidCircles[i].centerx + fluidCircles[i].radius * cos(radians(fluidCircles[i].angle));
          fluidCircles[i].ppy1 = fluidCircles[i].centery + fluidCircles[i].radius * sin(radians(fluidCircles[i].angle));
        }
        fluidCircles[i].angleNum = (fluidCircles[i].angleNum + 1) % (countCycle / 2);

        fluidCircles[i].angle = fluidCircles[i].angle + fluidCircles[i].direction * 360 / (countCycle / 2);

        fluidCircles[i].px1 = fluidCircles[i].centerx + fluidCircles[i].radius * cos(radians(fluidCircles[i].angle));
        fluidCircles[i].py1 = fluidCircles[i].centery + fluidCircles[i].radius * sin(radians(fluidCircles[i].angle));

        vx = (fluidCircles[i].ppx1 - fluidCircles[i].px1) * 4 * noise(fluidCircles[i].px1, fluidCircles[i].py1);
        vy = (fluidCircles[i].ppy1 - fluidCircles[i].py1) * 4 * noise(fluidCircles[i].ppx1, fluidCircles[i].ppy1);

        fluid.addVelocity(fluidCircles[i].px1, fluidCircles[i].py1, random(20, 30), vx, vy);

        fluidCircles[i].ppx1 = fluidCircles[i].px1;
        fluidCircles[i].ppy1 = fluidCircles[i].py1;
      }
      count = (count + 1) % countCycle;
    }
  }
}



public void settings() {
  size(viewport_w, viewport_h, P2D);
  smooth(4);
}

public void setup() {
  try {
    mindData = new ArrayList<EEG>();

    // help to test the path of file
    //File file = new File("");
    //System.out.println("Absolute Path: " + file.getAbsolutePath());
    //System.out.println("Canonical Path: " + file.getCanonicalPath());

    FileReader file = new FileReader("Documents/Processing/fluid_left/data/test1.txt");

    BufferedReader in = new BufferedReader(file);

    String buffer;
    String[] buffers;
    while ((buffer = in.readLine()) != null) {
      buffers = buffer.split(";");
      mindData.add(new EEG(
        Integer.parseInt(buffers[2]), 
        Integer.parseInt(buffers[3]), 
        Integer.parseInt(buffers[4]), 
        Integer.parseInt(buffers[5]), 
        Integer.parseInt(buffers[6]), 
        Integer.parseInt(buffers[7]), 
        Integer.parseInt(buffers[8]), 
        Integer.parseInt(buffers[9])));
    }
  } 
  catch (IOException e) {
    e.printStackTrace();
  }

  surface.setLocation(viewport_x, viewport_y);

  // main library context
  context = new DwPixelFlow(this);

  // visualization of the velocity field
  streamlines = new DwFluidStreamLines2D(context);

  String dir = "data/";

  customstreamlinerenderer = context.createShader(
    dir+"streamlineRender_Custom.vert", 
    dir+"streamlineRender_Custom.frag");

  // fluid simulation
  fluid = new DwFluid2D(context, viewport_w, viewport_h, fluidgrid_scale);

  // set some simulation parameters
  fluid.param.dissipation_density     = 0.5;
  fluid.param.dissipation_velocity    = 0.99f;
  fluid.param.dissipation_temperature = 0.30f;
  fluid.param.vorticity               = 0.10f;

  // pgraphics for fluid
  pg_fluid = (PGraphics2D) createGraphics(viewport_w, viewport_h, P2D);

  pg_fluid.smooth(4);

  //pgraphics for obstacles
  pg_obstacles = (PGraphics2D) createGraphics(viewport_w, viewport_h, P2D);
  pg_obstacles.noSmooth();
  pg_obstacles.beginDraw();
  pg_obstacles.clear();

  // border-obstacle
  pg_obstacles.strokeWeight(20);
  pg_obstacles.stroke(64);
  pg_obstacles.noFill();
  pg_obstacles.rect(0, 0, pg_obstacles.width, pg_obstacles.height);

  pg_obstacles.rectMode(CENTER);
  pg_obstacles.noStroke();
  pg_obstacles.fill(64);
  randomSeed(0);
  for (int i = 0; i < 20; i++) {
    float px = random(width);
    float py = random(height);
    float sx = random(15, 60);
    float sy = random(15, 60);
    pg_obstacles.rect(px, py, sx, sy);
  }

  pg_obstacles.endDraw();

  frameRate(30);
}


public void draw() {   
  if (count == 0) {
    dataIndex = (dataIndex + 1) % mindData.size();
    data = mindData.get(dataIndex);
    fluidCircles[0] = new FluidCircle(map(data.lowGamma, 0, 100, 150, 250));
    fluidCircles[1] = new FluidCircle(map(data.highGamma, 0, 100, 250, 350));
  }

  // update simulation
  fluid.addObstacles(pg_obstacles);
  fluid.update();

  // clear render target
  pg_fluid.beginDraw();
  pg_fluid.background(BACKGROUND_COLOR);
  pg_fluid.endDraw();

  // change color
  customstreamlinerenderer.begin();
  customstreamlinerenderer.uniform3f("lineColor", 0.8, 0.8, 0.8);
  customstreamlinerenderer.end();

  streamlines.shader_streamlineRender = customstreamlinerenderer;
  streamlines.render(pg_fluid, fluid, STREAMLINE_DENSITY);

  // RENDER
  // display textures
  image(pg_fluid, 0, 0);
  image(pg_obstacles, 0, 0);
}


public void fluid_reset() {
  // reset update method
  fluid.addCallback_FluiData(new  DwFluid2D.FluidData() {
    public void update(DwFluid2D fluid) {
    }
  }
  );
  fluid.reset();

  count = -1;
}

public void fluid_start() {
  // interface for adding data to the fluid simulation
  MyFluidData cb_fluid_data = new MyFluidData();
  fluid.addCallback_FluiData(cb_fluid_data);

  count = 0;
}

public void keyReleased() {
  if (key == 'r') {       // restart simulation
    fluid_reset();
  }
  if (key == '1') { // init fluid circle
    fluid_start();
  }
}

class FluidCircle {
  float angle;
  int angleNum = 0;
  float radius;
  float centerx, centery;
  float direction = 1.0;
  float ppx1 = 0, ppy1 = 0, ppx2 = 0, ppy2 = 0;
  float px1, py1, px2, py2;

  FluidCircle(float radius) {
    this.radius = radius;
    centerx = random(radius, width - radius);
    centery = random(radius, height - radius);

    angle = random(360);
    
    direction = random(1) > 0.5 ? 1.0 : -1.0;
  }
}

class EEG {
  float delta = 0.0;
  float theta = 0.0;
  float lowAlpha = 0.0;
  float highAlpha = 0.0;
  float lowBeta = 0.0;
  float highBeta = 0.0;
  float lowGamma = 0.0;
  float highGamma = 0.0;

  EEG() {
  }

  EEG(int delta, 
    int theta, 
    int lowAlpha, 
    int highAlpha, 
    int lowBeta, 
    int highBeta, 
    int lowGamma, 
    int highGamma) {
    this.delta = map(delta, 0, 3000000, 0, 100);
    this.theta = map(theta, 10000, 300000, 0, 100);
    this.lowAlpha = map(lowAlpha, 0, 350000, 200, 400);
    this.highAlpha = map(highAlpha, 0, 420000, 400, 500);
    this.lowBeta = map(lowBeta, 0, 250000, 0, 100);
    this.highBeta = map(highBeta, 0, 650000, 0, 100);
    this.lowGamma = map(lowGamma, 0, 1500000, 0, 100);
    this.highGamma = map(highGamma, 0, 120000, 0, 100);
  }
}
