package processingDrawFromDB.fluidSimulation;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.fluid.DwFluid2D;
import dataCollector.DBWriterAndReader;
import dataCollector.EEGData;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.opengl.PGraphics2D;

import java.io.File;
import java.sql.Timestamp;

public class FluidCenterDB extends PApplet {
    String PATH = new File("").getAbsolutePath();

    //    fluid state, 0 represent moving, bigger than 0 represent time of no data
    int state = 0;

    DBWriterAndReader dbWriterAndReader = DBWriterAndReader.getInstance();
    Timestamp lastTimeStamp = new Timestamp(System.currentTimeMillis());

    // fluid simulation
    DwFluid2D fluid;

    // render targets
    PGraphics2D pg_fluid;
    PImage pg;
    PGraphics surfaceGraphics;

    // parameter of background image
    int WIDTH = 2560;
    int HEIGHT = 1600;
    float CENTER_RADIUS = 675;

    int particleSize = 6;
    Particle[] particles = new Particle[particleSize];

    // data parameter
    EEG data = new EEG();

    // circle parameter
    int circleColorScale = 3;
    int circleBlurAlpha = 255;

    // fluid parameter
    int[] colors = new int[]{
            color(255, 109, 82),
            color(168, 178, 195),
            color(185, 244, 187)
    };

    // determine the occurance way of the fluid
    int count = 0;
    int countCycle = 60;

    public static void main (String[] args) {
        PApplet.main("processingDrawFromDB.fluidSimulation.FluidCenterDB");
    }

    public void settings () {
        size(1440, 900, P2D);
//        fullScreen(P2D);
    }

    public void setup () {
        //set up surface graphics
        surfaceGraphics = createGraphics(width, height);
        pg = loadImage(PATH + "\\src\\main\\java\\processingDrawFromDB\\fluidSimulation\\data\\bg.png");
        pg.resize(width, height);
        CENTER_RADIUS = (float) (CENTER_RADIUS * (height / 1600.0));

        // library context
        DwPixelFlow context = new DwPixelFlow(this);

        // fluid simulation
        fluid = new DwFluid2D(context, width, height, 1);

        // some fluid parameters
        fluid.param.dissipation_velocity = 0.70f;
        fluid.param.dissipation_density = 0.99f;

        // render-target
        pg_fluid = (PGraphics2D) createGraphics(width, height, P2D);

        frameRate(60);
    }


    public void draw () {
        if (count % countCycle == 0) {
            EEGData eegData = dbWriterAndReader.readLastData();
            if (eegData.getOccurTime().getTime() > lastTimeStamp.getTime()) {
                lastTimeStamp = eegData.getOccurTime();
                data = new EEG(eegData.getDelta(), eegData.getTheta(), eegData.getLowAlpha(), eegData.getHighAlpha(), eegData.getLowBeta(), eegData.getHighBeta(), eegData.getLowGamma(), eegData.getHighGamma());
                if (state > 10)
                    fluidStart();
                state = 0;
            } else {
                state++;

                if (state > 10)
                    fluidReset();
            }
        }

        // update simulation
        fluid.update();

        // clear render target
        pg_fluid.beginDraw();
        pg_fluid.background(255, 255, 255);
        pg_fluid.endDraw();

        // render fluid stuff
        fluid.renderFluidTextures(pg_fluid, 0);

        if (count > 0) {
            maxNoise = maxNoise + (map(data.delta + data.theta, 0, 200, 300, 700) - maxNoise) / (countCycle - count % countCycle);
        }

        // render circle outside
        renderSurfaceGraphics();

        // display
        image(pg_fluid, 0, 0);
        image(pg, 0, 0);
        image(surfaceGraphics, 0, 0);
    }

    int step = 20;
    float inter = 15; // difference between the sizes of two blobs
    float maxNoise = 0;
    float kMax = 1;
    int n = 20; // num of circles

    void renderSurfaceGraphics () {
        surfaceGraphics.beginDraw();

        // color of the circle line
        surfaceGraphics.background(39, 39, 39, 0);

        surfaceGraphics.stroke(204, 204, 204);
        float t = (float) (frameCount / 100.0);

        for (float i = n; i > 0; i--) {
            float size = CENTER_RADIUS + i * inter;
            float k = kMax * sqrt(i / n);
            float noisiness = maxNoise * (i / n);

            blob(size, width / 2, height / 2, k, t - i * step, noisiness);
        }

        surfaceGraphics.beginShape();
        surfaceGraphics.noStroke();
        int c = 255 - count * circleColorScale > 0 ? 255 - count * circleColorScale : 0;
        circleBlurAlpha = circleBlurAlpha + (int) ((map(data.lowAlpha + data.highAlpha, 600, 900, 0, 200) - circleBlurAlpha) / (countCycle - count % countCycle));
        circleBlurAlpha = c > 100 ? c : circleBlurAlpha;
        surfaceGraphics.fill(255, 255, 255, circleBlurAlpha);
        surfaceGraphics.ellipseMode(CENTER);
        surfaceGraphics.circle(width / (float) 2.0, height / (float) 2.0, CENTER_RADIUS * 2);
        surfaceGraphics.endShape();

        surfaceGraphics.endDraw();
    }

    void blob (float size, float xCenter, float yCenter, float k, float t, float noisiness) {
        surfaceGraphics.beginShape();
        surfaceGraphics.noFill();
        float angleStep = TWO_PI / 12;
        for (float theta = 0; theta <= TWO_PI + 2 * angleStep; theta += angleStep) {
            float r1, r2;
            r1 = cos(theta) + 1;
            r2 = sin(theta) + 1;
            float r = size + noise(k * r1, k * r2, t) * noisiness;
            float x = xCenter + r * cos(theta);
            float y = yCenter + r * sin(theta);
            surfaceGraphics.curveVertex(x, y);
        }
        surfaceGraphics.endShape();
    }

    void fluidReset () {
        // setup parameter of outer graphics
        inter = 15; // difference between the sizes of two blobs
        maxNoise = 0;
        kMax = 1;

        // reset update method
        fluid.addCallback_FluiData(new DwFluid2D.FluidData() {
                                       public void update (DwFluid2D fluid) {
                                       }
                                   }
        );
        fluid.reset();

        count = 0;
    }

    void fluidStart () {
        // setup parameter of outer graphics
        inter = (float) 0.05; // difference between the sizes of two blobs
        maxNoise = 500;
        kMax = random((float) 0.9, (float) 1.0);

        // init particles
        for (int i = 0; i < particleSize; i++) {
            int c1 = colors[i % colors.length];

            particles[i] = new Particle(c1, random(50, 70));
        }

        // implement updating method
        fluid.addCallback_FluiData(new DwFluid2D.FluidData() {
                                       public void update (DwFluid2D fluid) {
                                           if (frameCount % 3 == 0) {
                                               for (int i = 0; i < particleSize; i++) {
                                                   if (particles[i].point.x > width || particles[i].point.x < 0 || particles[i].point.y > height || particles[i].point.y < 0) {
                                                       particles[i].point = new PVector(random(width / 2 - CENTER_RADIUS, width / 2 + CENTER_RADIUS), random(height / 2 - CENTER_RADIUS, height / 2 + CENTER_RADIUS));
                                                   }

                                                   float scale;
                                                   if (i % 2 == 0) {
                                                       scale = data.lowAlpha;
                                                   } else {
                                                       scale = data.highAlpha;
                                                   }
                                                   float angle = noise(particles[i].point.x / scale, particles[i].point.y / scale, particles[i].radius) * TWO_PI * scale;

                                                   PVector vel = new PVector(cos(angle), sin(angle));
                                                   vel.mult(scale);

                                                   fluid.addVelocity(particles[i].point.x, particles[i].point.y, particles[i].radius, vel.x, vel.y);

                                                   fluid.addDensity(particles[i].point.x, particles[i].point.y, particles[i].radius, (float) (red(particles[i].c1) / 255.0), (float) (green(particles[i].c1) / 255.0), (float) (blue(particles[i].c1) / 255.0), 1.0f);
                                                   particles[i].point.add(vel);
                                               }
                                               count++;
                                           }
                                       }
                                   }
        );
    }

    class Particle {
        PVector point;
        int c1;
        float radius;
        int radiansCount = 0;

        Particle (float x, float y, int c1, float radius) {
            point = new PVector(x, y);
            this.c1 = c1;
            this.radius = radius;
            this.radiansCount = (int) random(360);
        }

        Particle (int c1, float radius) {
            changePoint();
            this.c1 = c1;
            this.radius = radius;
            this.radiansCount = (int) random(360);
        }

        void changePoint () {
            radiansCount = (radiansCount + 5) % 360;
            point = new PVector(width / 2 + (CENTER_RADIUS + 100 * random(1)) * cos(radians(this.radiansCount)), height / 2 + (CENTER_RADIUS + 100 * random(1)) * sin(radians(this.radiansCount)));
            //point = new PVector(width / 2 + CENTER_RADIUS * cos(radians(this.radiansCount)), height / 2 + CENTER_RADIUS * sin(radians(this.radiansCount)));
        }
    }

    class EEG {
        float delta = (float) 0.0;
        float theta = (float) 0.0;
        float lowAlpha = (float) 0.0;
        float highAlpha = (float) 0.0;
        float lowBeta = (float) 0.0;
        float highBeta = (float) 0.0;
        float lowGamma = (float) 0.0;
        float highGamma = (float) 0.0;

        EEG () {
        }

        EEG (int delta,
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
