package processingDrawFromDB.fluidLeft;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLSLProgram;
import com.thomasdiewald.pixelflow.java.fluid.DwFluid2D;
import com.thomasdiewald.pixelflow.java.fluid.DwFluidStreamLines2D;
import dataCollector.DBWriterAndReader;
import dataCollector.EEGData;
import processing.core.PApplet;
import processing.opengl.PGraphics2D;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;

public class FluidLeft extends PApplet{
    DBWriterAndReader dbWriterAndReader = DBWriterAndReader.getInstance();
    Timestamp lastTimeStamp = new Timestamp(System.currentTimeMillis());

    String PATH = new File("").getAbsolutePath();
    //    fluid state, 0 represent moving, bigger than 0 represent time of no data
    int state = 0;

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
    EEG data = new EEG();

    // some state variables for the GUI/display
    int     BACKGROUND_COLOR           = 39;
    int     STREAMLINE_DENSITY         = 15;

    // image control
    FluidCircle[] fluidCircles = new FluidCircle[2];

    int count = 0;
    int countCycle = 60;

    public static void main (String[] args) {
        PApplet.main("processingDrawFromDB.fluidLeft.FluidLeft");
    }

    private class MyFluidData implements DwFluid2D.FluidData {
        // update() is called during the fluid-simulation update step.
        public void update(DwFluid2D fluid) {

            float px, py, vx, vy, vscale;

            if (frameCount % 5 == 0) {
                //yi duan yuan hu, change direction after one round
                for (int i = 0; i < 2; i++) {
                    if (fluidCircles[i].angleNum == 0) { //<>//
                        fluidCircles[i].direction = fluidCircles[i].direction * -1.0f;

                        fluidCircles[i].ppx1 = fluidCircles[i].centerx + fluidCircles[i].radius * cos(radians(fluidCircles[i].angle));
                        fluidCircles[i].ppy1 = fluidCircles[i].centery + fluidCircles[i].radius * sin(radians(fluidCircles[i].angle));
                    }
                    fluidCircles[i].angleNum = (fluidCircles[i].angleNum + 1) % countCycle;

                    fluidCircles[i].angle = fluidCircles[i].angle + fluidCircles[i].direction * 360 / countCycle;

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
        surface.setLocation(viewport_x, viewport_y);

        // main library context
        context = new DwPixelFlow(this);

        // visualization of the velocity field
        streamlines = new DwFluidStreamLines2D(context);

        String dir = PATH + "\\src\\main\\java\\fluidfrag\\";

        customstreamlinerenderer = context.createShader(
                dir+"streamlineRender_Custom.vert",
                dir+"streamlineRender_Custom.frag");

        // fluid simulation
        fluid = new DwFluid2D(context, viewport_w, viewport_h, fluidgrid_scale);

        // set some simulation parameters
        fluid.param.dissipation_density     = 0.5f;
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
            EEGData eegData = dbWriterAndReader.readLastData();
            if (eegData.getOccurTime().getTime() > lastTimeStamp.getTime()) {
                lastTimeStamp = eegData.getOccurTime();
                data = new EEG(eegData.getDelta(), eegData.getTheta(), eegData.getLowAlpha(), eegData.getHighAlpha(), eegData.getLowBeta(), eegData.getHighBeta(), eegData.getLowGamma(), eegData.getHighGamma());
                fluidCircles[0] = new FluidCircle(map(data.lowBeta, 0, 100, 150, 250));
                fluidCircles[1] = new FluidCircle(map(data.highBeta, 0, 100, 250, 350));
                if (state > 5)
                    fluid_start();
                state = 0;
            } else {
                state++;

                if (state > 5)
                    fluid_reset();
            }
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
        customstreamlinerenderer.uniform3f("lineColor", 0.8f, 0.8f, 0.8f);
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

        count = 0;
    }

    public void fluid_start() { //<>//
        // interface for adding data to the fluid simulation
        MyFluidData cb_fluid_data = new MyFluidData();
        fluid.addCallback_FluiData(cb_fluid_data);

        count = 0;
    }

    class FluidCircle {
        float angle;
        int angleNum = 0;
        float radius;
        float centerx, centery;
        float direction = 1.0f;
        float ppx1 = 0, ppy1 = 0, ppx2 = 0, ppy2 = 0;
        float px1, py1, px2, py2;

        FluidCircle(float radius) {
            this.radius = radius;
            centerx = random(radius, width - radius);
            centery = random(radius, height - radius);

            angle = random(360);

            direction = random(1) > 0.5f ? 1.0f : -1.0f;
        }
    }

    class EEG {
        float delta = 0.0f;
        float theta = 0.0f;
        float lowAlpha = 0.0f;
        float highAlpha = 0.0f;
        float lowBeta = 0.0f;
        float highBeta = 0.0f;
        float lowGamma = 0.0f;
        float highGamma = 0.0f;

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

}
