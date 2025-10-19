package net.anemoia.rivals.common.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ParticleDefinition {
    @SerializedName("particle_id")
    private String particleId;

    private ParticleProperties properties;

    public String getParticleId() { return particleId; }
    public ParticleProperties getProperties() { return properties; }

    public static class ParticleProperties {
        private ParticleTexture texture;
        private boolean gravity;
        private boolean collision;

        private ParticleSize size;

        @SerializedName("base_velocity")
        private ParticleVelocityRange baseVelocity;

        @SerializedName("base_rotation")
        private ParticleRotationRange baseRotation;

        public ParticleTexture getTexture() { return texture; }
        public boolean isGravity() { return gravity; }
        public boolean isCollision() { return collision; }
        public ParticleSize getSize() { return size; }
        public ParticleVelocityRange getBaseVelocity() { return baseVelocity; }
        public ParticleRotationRange getBaseRotation() { return baseRotation; }
    }

    public static class ParticleTexture {
        private String single;
        private List<String> multiple;

        @SerializedName("animation_type")
        private String animationType; // "random", "sequence", "loop"

        @SerializedName("frame_duration")
        private int frameDuration; // ticks per frame for sequence/loop

        // 3D Model support
        @SerializedName("model_path")
        private String modelPath; // Path to .obj, .json, or other 3D model file

        @SerializedName("model_texture")
        private String modelTexture; // Texture to apply to the 3D model

        @SerializedName("model_scale")
        private float modelScale; // Scale multiplier for the model

        @SerializedName("is_3d_model")
        private boolean is3dModel; // Flag to indicate this is a 3D model particle

        @SerializedName("model_hitbox")
        private ModelHitbox modelHitbox; // Hitbox definition for 3D models

        public String getSingle() { return single; }
        public List<String> getMultiple() { return multiple; }
        public String getAnimationType() { return animationType; }
        public int getFrameDuration() { return frameDuration; }
        public String getModelPath() { return modelPath; }
        public String getModelTexture() { return modelTexture; }
        public float getModelScale() { return modelScale; }
        public boolean is3dModel() { return is3dModel; }
        public ModelHitbox getModelHitbox() { return modelHitbox; }

        public boolean hasMultipleTextures() {
            return multiple != null && !multiple.isEmpty();
        }

        public boolean isModel() {
            return is3dModel && modelPath != null;
        }

        public boolean hasHitbox() {
            return modelHitbox != null;
        }
    }

    public static class ModelHitbox {
        @SerializedName("hitbox_type")
        private String hitboxType; // "box", "custom"

        private float width;
        private float height;
        private float depth;

        @SerializedName("offset_x")
        private float offsetX; // Hitbox offset from model center

        @SerializedName("offset_y")
        private float offsetY;

        @SerializedName("offset_z")
        private float offsetZ;

        @SerializedName("scale_with_model")
        private boolean scaleWithModel; // Whether hitbox scales with model_scale

        public String getHitboxType() { return hitboxType; }
        public float getWidth() { return width; }
        public float getHeight() { return height; }
        public float getDepth() { return depth; }
        public float getOffsetX() { return offsetX; }
        public float getOffsetY() { return offsetY; }
        public float getOffsetZ() { return offsetZ; }
        public boolean isScaleWithModel() { return scaleWithModel; }
    }

    public static class ParticleSize {
        private float start;
        private float end;

        public float getStart() { return start; }
        public float getEnd() { return end; }
    }

    public static class ParticleColor {
        private String start;
        private String end;

        public String getStart() { return start; }
        public String getEnd() { return end; }
    }

    public static class ParticleVelocityRange {
        private VelocityRange x;
        private VelocityRange y;
        private VelocityRange z;

        public VelocityRange getX() { return x; }
        public VelocityRange getY() { return y; }
        public VelocityRange getZ() { return z; }
    }

    public static class VelocityRange {
        private float min;
        private float max;

        public float getMin() { return min; }
        public float getMax() { return max; }
    }

    public static class ParticleRotationRange {
        @SerializedName("static_rotation")
        private StaticRotation staticRotation;

        @SerializedName("rotation_speed")
        private RotationRange rotationSpeed;

        @SerializedName("rotation_axis")
        private String rotationAxis; // "x", "y", "z", "xy", "xz", "yz", "xyz", or "all"

        @SerializedName("initial_rotation")
        private RotationRange initialRotation;

        public StaticRotation getStaticRotation() { return staticRotation; }
        public RotationRange getRotationSpeed() { return rotationSpeed; }
        public String getRotationAxis() { return rotationAxis; }
        public RotationRange getInitialRotation() { return initialRotation; }

        public boolean hasStaticRotation() {
            return staticRotation != null;
        }

        public boolean hasDynamicRotation() {
            return rotationSpeed != null && (rotationSpeed.getMin() != 0 || rotationSpeed.getMax() != 0);
        }

        public boolean rotatesOnAxis(char axis) {
            return rotationAxis != null && (rotationAxis.contains(String.valueOf(axis)) || rotationAxis.equals("all"));
        }
    }


    public static class StaticRotation {
        private float x; // pitch
        private float y; // yaw
        private float z; // roll

        public float getX() { return x; }
        public float getY() { return y; }
        public float getZ() { return z; }
    }

    public static class RotationRange {
        private float min;
        private float max;

        public float getMin() { return min; }
        public float getMax() { return max; }
    }
}
