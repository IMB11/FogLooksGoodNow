package com.mineblock11.foglooksgoodnow.utils;

import com.ibm.icu.impl.Pair;
import org.joml.Vector3f;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/* cappin's math utils
 * hope you find this useful :) */
public class MathUtils {
    private static final float[] ASIN = Util.make(new float[65536], (array) -> {
        for(int i = 0; i < array.length; ++i) {
            array[i] = (float)Math.sin((double)i * Math.PI * 2.0D / (double)array.length);
        }
    });

    public static float arcsin(float pValue) {
        return ASIN[(int)(pValue * 10430.378F) & '\uffff'];
    }
    public static float arccos(float pValue) { return (float) ((arcsin(pValue) * -1) + (Math.PI / 2));}

    public static Vec3d vec3Lerp(float delta, Vec3d start, Vec3d end) {
        return new Vec3d(MathHelper.lerp(delta, start.getX(), end.getX()), MathHelper.lerp(delta, start.getY(), end.getY()), MathHelper.lerp(delta, start.getZ(), end.getZ()));
    }

    public static Vec3d vec3Lerp(double delta, Vec3d start, Vec3d end) {
        return new Vec3d(MathHelper.lerp(delta, start.getX(), end.getX()), MathHelper.lerp(delta, start.getY(), end.getY()), MathHelper.lerp(delta, start.getZ(), end.getZ()));
    }

    public static Vec3d rotateVectorY(Vec3d vector, float rotation) {
        /*Vec3 nV = vector.normalize();
        double currentAngle = Mth.atan2(nV.z, nV.x);
        double currentRadius = vector.multiply(1, 0, 1).length();
        double x = Mth.sin((float) currentAngle + rotation) * currentRadius;
        double z = Mth.cos((float) currentAngle + rotation) * currentRadius;

        return new Vec3(x, vector.y, z);*/
        return new Vec3d(vector.x * Math.cos(rotation) - vector.z * Math.sin(rotation), vector.y, vector.z * Math.cos(rotation) + vector.x * Math.sin(rotation));
    }



    /**
     * Maps one range of numbers to another. Incredibly useful function for lazy people like me.
     * @param fromMin The minimum of the range you're mapping from.
     * @param fromMax The maximum of the range you're mapping from.
     * @param toMin The minimum of the range you're mapping to.
     * @param toMax The maximum of the range you're mapping to.
     * @param value The value you're mapping.
     * @return The value, mapped to the second range.
     */
    public static float mapRange(float fromMin, float fromMax, float toMin, float toMax, float value) {
        return toMin + (((value - fromMin) * (toMax - toMin))/(fromMax - fromMin));
    }

    /**
     * Maps one range of numbers to another. Incredibly useful function for lazy people like me.
     * @param fromMin The minimum of the range you're mapping from.
     * @param fromMax The maximum of the range you're mapping from.
     * @param toMin The minimum of the range you're mapping to.
     * @param toMax The maximum of the range you're mapping to.
     * @param value The value you're mapping.
     * @return The value, mapped to the second range.
     */
    public static double mapRange(double fromMin, double fromMax, double toMin, double toMax, double value) {
        return toMin + (((value - fromMin) * (toMax - toMin))/(fromMax - fromMin));
    }


    /**
     * Biases an input so that smaller numbers are more likely to occur.
     * @param value The input value. Should range for 0 - 1; any more or less will break it.
     * @param bias The amount of biasing applied to the function. I like to keep it around 0.5.
     * @return The input with biasing applied.
     */
    public static float bias (float value, float bias) {
        float b = (float) Math.pow(1-bias, 3);
        return (value*b)/(value*b-value+1);
    }

    /**
     * Biases an input so that smaller numbers are more likely to occur.
     * @param value The input value. Should range for 0 - 1; any more or less will break it.
     * @param bias The amount of biasing applied to the function. I like to keep it around 0.5.
     * @return The input with biasing applied.
     */
    public static double bias (double value, double bias) {
        if (bias == 0) return value;
        double b = Math.pow(1-bias, 3);
        return (value*b)/(value*b-value+1);
    }

    public static float biasTowardsIntegers(float value) {
        float mod = value % 1;
        return (float) ((mod * mod * mod * mod) + Math.floor(value));
    }

    /**
     * Takes an input value and biases it using a sine function towards two larger magnitude values.
     *
     * @param value A value in the range [-1, 1]
     * @param bias The effect of the bias. At {@code 0.0}, there will be no bias. Mojang only uses {@code 1.0} here.
     * @param iterations Allows you to repeat the effect several times.
     */
    public static double biasTowardsExtreme(double value, double bias, int iterations) {
        double result = value;
        for (int i = 0; i < iterations; i++) {
            result = result + Math.sin(Math.PI * result) * bias / Math.PI;
        }
        return result;
    }

    /**
     * Determines if a value is within range of another.
     * @param value The value you want to check the closeness of.
     * @param input The value you want to check it against.
     * @param margin The range to check within.
     * @return If the value is within range of the input.
     */
    public static boolean within (float value, float input, float margin) {
        float difference = Math.abs(value - input);
        return difference <= Math.abs(margin);
    }

    /**
     * Get a random float between two numbers. Rather self-explanatory.
     * @param rand The RNG used to determine the float.
     * @param min The minimum value of the random number.
     * @param max The maximum value of the random number.
     * @return A random float between the minimum and the maximum.
     */
    public static float getRandomFloatBetween (Random rand, float min, float max) {
        return mapRange(0, 1, min, max, rand.nextFloat());
    }
    public static float getRandomFloatBetween (net.minecraft.util.math.random.Random rand, float min, float max) {
        return mapRange(0, 1, min, max, rand.nextFloat());
    }

    public static float map(float min, float max, float value) {
        return value * (max - min) + min;
    }
    public static double map(double min, double max, double value) {
        return value * (max - min) + min;
    }

    public static Vec3d adjustAxis(Vec3d vector, double desiredLength, Direction.Axis axis) {
        double lSqr = desiredLength * desiredLength;
        switch (axis) {
            case X:
                return new Vec3d(Math.sqrt(lSqr + (-1 * vector.getY() * vector.getY()) + (-1 * vector.getZ() * vector.getZ())), vector.getY(), vector.getZ());
            case Y:
                return new Vec3d(vector.getX(), Math.sqrt(lSqr + (-1 * vector.getX() * vector.getX()) + (-1 * vector.getZ() * vector.getZ())), vector.getZ());
            default:
                return new Vec3d(vector.getX(), vector.getY(), Math.sqrt(lSqr + (-1 * vector.getX() * vector.getX()) + (-1 * vector.getY() * vector.getY())));
        }
    }

    public static float awfulRandom(float seed) {
        return MathHelper.fractionalPart(MathHelper.sin((float) (seed * Math.tan(MathHelper.sqrt((float) Math.abs(seed - seed * 0.5F))))) * 100000.0F);
    }

    /**
     * Applies a series of distinct stairs (terraces) to a number. Very useful for terrain.
     * @param height A float between 0 and 1. The input height that you want to be terraced.
     * @param width A float between 0 and 1. The width of each "step" on the terrace.
     * @param erosion A float between 0 and 1. The factor of influence for the terrace.
     * @return The inputted height with the terraced step effect applied.
     */
    public static Pair<Float, Boolean> terrace (float height, float width, float erosion, float terraceThreshold) {
        float terraceWidth = width * 0.5f;
        if (terraceWidth == 0) {
            terraceWidth += 0.0001f;
        }

        float k = (float) Math.floor(height / terraceWidth);
        float f = (height - k * terraceWidth) / terraceWidth;
        float s = Math.min(2.0f * f, 1.0f);

        float secondTerraceThreshold = Math.abs(terraceThreshold - 1);
        boolean isTerrace = s >= terraceThreshold || s <= secondTerraceThreshold;

        return Pair.of(MathHelper.lerp(erosion,(k + s) * terraceWidth, height), isTerrace);
    }

    /**
     * Applies a series of distinct stairs (terraces) to a number. Very useful for terrain.
     * @param height A float between 0 and 1. The input height that you want to be terraced.
     * @param width A float between 0 and 1. The width of each "step" on the terrace.
     * @param erosion A float between 0 and 1. The factor of influence for the terrace.
     * @return The inputted height with the terraced step effect applied.
     */
    public static Pair<Float, Float> terrace (float height, float width, float erosion) {
        float terraceWidth = width * 0.5f;
        if (terraceWidth == 0) {
            terraceWidth += 0.0001f;
        }

        float k = (float) Math.floor(height / terraceWidth);
        float f = (height - k * terraceWidth) / terraceWidth;
        float s = Math.min(2.0f * f, 1.0f);

        float terraceMultiplier = MathHelper.clamp(MathUtils.invert(Math.abs(s - 1.0F)), 0.0F, 1.0F);

        return Pair.of(MathHelper.lerp(erosion,(k + s) * terraceWidth, height), terraceMultiplier);
    }

    /**
     * Applies a series of distinct stairs (terraces) to a number. Very useful for terrain.
     * @param height A float between 0 and 1. The input height that you want to be terraced.
     * @param width A float between 0 and 1. The width of each "step" on the terrace.
     * @param erosion A float between 0 and 1. The factor of influence for the terrace.
     * @return The inputted height with the terraced step effect applied.
     */
    public static Pair<Double, Double> terrace (double height, double width, double erosion) {
        double terraceWidth = width * 0.5f;
        if (terraceWidth == 0) {
            terraceWidth += 0.0001f;
        }

        double k = (float) Math.floor(height / terraceWidth);
        double f = (height - k * terraceWidth) / terraceWidth;
        double s = Math.min(2.0f * f, 1.0f);

        double terraceMultiplier = MathHelper.clamp(MathUtils.invert(Math.abs(s - 1.0F)), 0.0F, 1.0F);

        return Pair.of(MathHelper.lerp(erosion,(k + s) * terraceWidth, height), terraceMultiplier);
    }

    /**
     * Inverts a number.
     * @param input The number to invert
     * @return 0 when the input is 1, and 1 when the input is 0.
     */
    public static float invert(float input) {
        return (1 - input) * -1;
    }

    /**
     * Inverts a number.
     * @param input The number to invert
     * @return 0 when the input is 1, and 1 when the input is 0.
     */
    public static double invert(double input) {
        return (1 - input) * -1;
    }

    /**
     * Quick and dirty power function. Should be faster than the regular Math.pow() function, if only slightly.
     * @param input
     * @param exponentIn
     * @return
     */
    public static float lazyPow(float input, int exponentIn) {
        float value = 1;
        for (int exponent = 0; exponent < exponentIn; exponent++) {
            value *= input;
        }

        return value;
    }

    /**
     * Quick and dirty power function. Should be faster than the regular Math.pow() function, if only slightly.
     * @param input
     * @param exponentIn
     * @return
     */
    public static double lazyPow(double input, int exponentIn) {
        double value = 1;
        for (int exponent = 0; exponent < exponentIn; exponent++) {
            value *= input;
        }

        return value;
    }


    public static float minMaxSin (float value, float min, float max) {
        return (((MathHelper.sin(value) + 1) * 0.5F) * (max - min)) + min;
    }

    public static float fastSin (float value) {
        float offset = (float) ((Math.ceil((1 /  Math.PI) * value) * 2) - 1);
        float v = (float) (-(4 / Math.pow(Math.PI, 2)) * Math.pow((value - (offset * (Math.PI / 2))), 2) + 1);

        if ((value % Math.PI * 2) / Math.PI < 1.0) {
            return v;
        } else {
            return -v;
        }
    }

    public static float wave (WaveType type, float x, float frequency, float amplitude) {
        float value = 0;
        switch (type) {
            case sine:
                value = (float) Math.sin((Math.PI * x) / frequency);
            case square:
                value = 2.0F * Math.round((Math.sin((Math.PI * x) / frequency) + 1.0F) / 2.0F) - 1.0F;
            case triangle:
                value = (-1 * ((((x + (frequency / 2)) % (2 * frequency)) - frequency) / frequency / 2)) + 1;
            case sawtooth:
                value = (((x + frequency) % (2 * frequency)) / frequency) - 1;
            default:
                value = x;
        }

        return value * amplitude;
    }

    public enum WaveType {
        sine, square, triangle, sawtooth;
    }

    /**
     * Returns the minimum of two values while reducing discontinuities in their derivatives.
     * See https://iquilezles.org/www/articles/smin/smin.htm
     *
     * @param value1 The first value you'd like to ta take the minimum of.
     * @param value2 The second value you'd like to ta take the minimum of.
     * @param smoothness The radius of the smoothing effect.
     * @return The smoothed minimum of the two values.
     */
    public static float smoothMin(float value1, float value2, float smoothness) {
        if (smoothness == 0) {
            return Math.min(value1, value2);
        } else {
            float h = value1 - value2;
            return (float) (0.5 * ((value1 + value2) - Math.sqrt(h * h + smoothness)));
        }
    }

    /**
     * Returns the maximum of two values while reducing discontinuities in their derivatives.
     * See https://iquilezles.org/www/articles/smin/smin.htm
     *
     * @param value1 The first value you'd like to ta take the maximum of.
     * @param value2 The second value you'd like to ta take the maximum of.
     * @param smoothness The radius of the smoothing effect.
     * @return The smoothed maximum of the two values.
     */
    public static float smoothMax(float value1, float value2, float smoothness) {
        float h = (float) (Math.max(smoothness - Math.abs(value1 - value2), 0.0 ) / smoothness);
        return (float) (Math.max(value1, value2) + h * h * h * smoothness * (1.0 / 6.0));
    }

    public static double smoothMinExpo(double a, double b, double smoothness) {
        if (smoothness == 0) {
            return Math.min(a, b);
        } else {
            double k = 1 / smoothness;
            double res = Math.pow(2, -k * a) + Math.pow(2, -k * b);
            return -MathUtils.log(2, res) / k;
        }
    }

    public static double smoothClampExpo(double value, double min, double max, double smoothness) {
        return smoothMinExpo(smoothMinExpo(value, max, smoothness), min, -smoothness);
    }

    public static double smoothMinExpo2(double smoothness, double... nums) {
        if (smoothness == 0) {
            double min = Double.MAX_VALUE;
            for (double num : nums) {
                min = Math.min(num, min);
            }

            return min;
        } else {
            double k = 1 / smoothness;
            double res = 0;
            for (double num : nums) {
                res += Math.pow(2, -k * num);
            }

            return -MathUtils.log(2, res) / k;
        }
    }


    public static double a_asin(double x) {
        double a0 = 1.5707288;
        double a1 = -0.2121144;
        double a2 = 0.0742610;
        double a3 = -0.0187293;

        double xx = Math.abs(x);

        var y = Math.PI/2 - Math.sqrt(1-xx)*(a0 + a1 * xx + a2 * xx * xx + a3 * xx * xx * xx);
        return y * Math.signum(x);
    }

    public static double a_acos(double x) {
        return (a_asin(x) * -1) + (Math.PI / 2);
    }

    private static double log(double base, double logNumber) {
        return Math.log10(logNumber) / Math.log10(base);
    }

    public static int[] getValidIndexes(Object array, int... excludedIndexes) {
        try {
            int[] retArray = new int[Array.getLength(array) - excludedIndexes.length];
            int index = 0;
            for (int i = 0; i < Array.getLength(array); i++) {
                if (!Arrays.asList(excludedIndexes).contains(i)) {
                    retArray[index] = i;
                    index++;
                }
            }

            return retArray;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public static Object reverseArray(Object array) {
        Object[] returnArray = new Object[Array.getLength(array)];
        for (int i = 0; i < Array.getLength(array); i++) {
            returnArray[Array.getLength(array) - i] = Array.get(array, i);
        }

        return returnArray;
    }

    public static float sobelFilter(float[][] heightmap, int x, int z) {
        float height = heightmap[x][z];
        int maxX = heightmap.length;
        int maxZ = heightmap[0].length;

        // Compute the differentials by stepping over 1 in both directions.
        float dx = heightmap[x + 1 < maxX ? x + 1 : x - 1][z] - height;
        float dz = heightmap[x][z + 1 < maxZ ? z + 1 : z - 1] - height;

        // The "steepness" is the magnitude of the gradient vector
        // For a faster but not as accurate computation, you can just use abs(dx) + abs(dy)
        return (float) Math.sqrt(dx * dx + dz * dz);
    }

    public static float triSobelFilter(float[][][] heightmap, int x, int y, int z) {
        float height = heightmap[x][y][z];
        int maxX = heightmap.length;
        int maxY = heightmap[0].length;
        int maxZ = heightmap[0][0].length;

        // Compute the differentials by stepping over 1 in both directions.
        float dx = heightmap[x + 1 < maxX ? x + 1 : x - 1][y][z] - height;
        float dy = heightmap[x][y + 1 < maxY ? y + 1 : y - 1][z] - height;
        float dz = heightmap[x][y][z + 1 < maxZ ? z + 1 : z - 1] - height;

        // The "steepness" is the magnitude of the gradient vector
        // For a faster but not as accurate computation, you can just use abs(dx) + abs(dy)
        return (float) Math.pow(dx * dx + dy * dy + dz * dz, 1/3);
    }

    public static Vec3d cubeNormalize(Vec3d input) {
        double divisor = 1 / Math.max(Math.max(Math.abs(input.x), Math.abs(input.y)),  Math.abs(input.z));
        return input.multiply(divisor, divisor, divisor);
    }

    //Refer to https://easings.net/.
    //X should usually be between zero and one, but it doesn't need to be.
    public static float ease(float x, EasingType easingType) {
        return easingType.ease(x);
    }

    public enum EasingType implements IEasingFunction {
        linear {
            public float ease(float x) { return x;}
        },
        easeInQuad {
            public float ease(float x) {
                return x * x;
            }
        },
        easeOutQuad {
            public float ease(float x) {
                return 1 - (1 - x) * (1 - x);
            }
        },
        easeInOutQuad {
            public float ease(float x) {
                return x < 0.5 ? 2 * x * x : (float) (1 - Math.pow(-2 * x + 2, 2) / 2);
            }
        },
        easeInCubic {
            public float ease(float x) {
                return x * x * x;
            }
        },
        easeOutCubic {
            public float ease(float x) {
                return (float) (1 - Math.pow(1 - x, 3));
            }
        },
        easeInOutCubic {
            public float ease(float x) {
                return x < 0.5 ? 4 * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 3) / 2);
            }
        },
        easeInQuart {
            public float ease(float x) {
                return x * x * x * x;
            }
        },
        easeOutQuart {
            public float ease(float x) {
                return (float) (1 - Math.pow(1 - x, 4));
            }
        },
        easeInOutQuart {
            public float ease(float x) {
                return x < 0.5 ? 8 * x * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 4) / 2);
            }
        },
        easeInQuint {
            public float ease(float x) {
                return x * x * x * x * x;
            }
        },
        easeOutQuint {
            public float ease(float x) {
                return (float) (1 - Math.pow(1 - x, 5));
            }
        },
        easeInOutQuint {
            public float ease(float x) {
                return x < 0.5 ? 16 * x * x * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 5) / 2);
            }
        },
        easeInSine {
            public float ease(float x) {
                return 1 - MathHelper.cos((float) ((x * Math.PI) / 2));
            }
        },
        easeOutSine {
            public float ease(float x) {
                return MathHelper.sin((float) ((x * Math.PI) / 2));
            }
        },
        easeInOutSine {
            public float ease(float x) {
                return -(MathHelper.cos((float) (Math.PI * x)) - 1) / 2;
            }
        },
        easeInExpo {
            public float ease(float x) {
                return x == 0 ? 0 : (float) Math.pow(2, 10 * x - 10);
            }
        },
        easeOutExpo {
            public float ease(float x) {
                return x == 1 ? 1 : (float) (1 - Math.pow(2, -10 * x));
            }
        },
        easeInOutExpo {
            public float ease(float x) {
                return x == 0
                        ? 0
                        : (float) (x == 1
                        ? 1
                        : x < 0.5
                        ? Math.pow(2, 20 * x - 10) / 2
                        : (2 - Math.pow(2, -20 * x + 10)) / 2);
            }
        },
        easeInCirc {
            public float ease(float x) {
                return (float) (1 - Math.sqrt(1 - Math.pow(x, 2)));
            }
        },
        easeOutCirc {
            public float ease(float x) {
                return (float) Math.sqrt(1 - Math.pow(x - 1, 2));
            }
        },
        easeInOutCirc {
            public float ease(float x) {
                return (float) (x < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2);
            }
        },
        easeInBack {
            public float ease(float x) {
                return 2.70158F * x * x * x - 1.70158F * x * x;
            }
        },
        easeOutBack {
            public float ease(float x) {
                return (float) (1 + 2.70158F * Math.pow(x - 1, 3) + 1.70158F * Math.pow(x - 1, 2));
            }
        },
        easeInOutBack {
            public float ease(float x) {
                return (float) (x < 0.5
                        ? (Math.pow(2 * x, 2) * ((2.5949095F + 1) * 2 * x - 2.5949095F)) / 2
                        : (Math.pow(2 * x - 2, 2) * ((2.5949095F + 1) * (x * 2 - 2) + 2.5949095F) + 2) / 2);
            }
        },
        easeInElastic {
            public float ease(float x) {
                return x == 0
                        ? 0
                        : (float) (x == 1
                        ? 1
                        : -Math.pow(2, 10 * x - 10) * MathHelper.sin((float) ((x * 10 - 10.75) * ((2 * Math.PI) / 3))));
            }
        },
        easeOutElastic {
            public float ease(float x) {
                return x == 0
                        ? 0
                        : (float) (x == 1
                        ? 1
                        : Math.pow(2, -10 * x) * MathHelper.sin((float) ((x * 10 - 0.75) * ((2 * Math.PI) / 3))) + 1);
            }
        },
        easeInOutElastic {
            public float ease(float x) {
                return x == 0
                        ? 0
                        : (float) (x == 1
                        ? 1
                        : x < 0.5
                        ? -(Math.pow(2, 20 * x - 10) * MathHelper.sin((float) ((20 * x - 11.125) * ((2 * Math.PI) / 4.5)))) / 2
                        : (Math.pow(2, -20 * x + 10) * MathHelper.sin((float) ((20 * x - 11.125) * ((2 * Math.PI) / 4.5)))) / 2 + 1);
            }
        },
        easeInBounce {
            public float ease(float x) {
                return 1 - bounceOut(1 - x);
            }
        },
        easeOutBounce {
                public float ease(float x) {
                    return 1 - bounceOut(1 - x);
                }
        },
        easeInOutBounce {
            public float ease(float x) {
                return x < 0.5
                        ? (1 - bounceOut(1 - 2 * x)) / 2
                        : (1 + bounceOut(2 * x - 1)) / 2;
            }
        };

        private static float bounceOut(float x) {
            float n1 = 7.5625F;
            float d1 = 2.75F;

            if (x < 1 / d1) {
                return n1 * x * x;
            } else if (x < 2 / d1) {
                return n1 * (x -= 1.5 / d1) * x + 0.75F;
            } else if (x < 2.5 / d1) {
                return n1 * (x -= 2.25 / d1) * x + 0.9375F;
            } else {
                return n1 * (x -= 2.625 / d1) * x + 0.984375F;
            }
        }
    }
    
    public interface IEasingFunction {
        float ease(float x);
    }

    public static float triLerp(Vector3f pct, float cAAA, float cAAB, float cABA, float cABB, float cBAA, float cBAB, float cBBA, float cBBB) {
        float cAA = MathHelper.lerp(pct.x(), cAAA, cBAA);
        float cAB = MathHelper.lerp(pct.x(), cAAB, cBAB);
        float cBB = MathHelper.lerp(pct.x(), cABB, cBBB);
        float cBA = MathHelper.lerp(pct.x(), cABA, cBBA);
        
        float cA = MathHelper.lerp(pct.z(), cAA, cBA);
        float cB = MathHelper.lerp(pct.z(), cAB, cBB);
        
        float c = MathHelper.lerp(pct.y(), cA, cB);
        
        return c;
    }

    /** Find a minimum point for a bounded function. May be a local minimum.
     * minX      : the smallest input value
     * maxX      : the largest input value
     * function  : a function that returns a value `y` given an `x`
     * threshold : how close in `x` the bounds must be before returning
     * returns   : the `x` value that produces the smallest `y`
     */
    public static double localMinimum(double minX, double maxX, Function<Double, Double> function, double threshold) {
        if (Double.isNaN(threshold)) { threshold = 1e-10; }
        double m = minX;
        double n = maxX;
        double k = (n + m) / 2;

        while ((n - m) > threshold) {
            k = (n + m) / 2;
            if (function.apply(k - threshold) < function.apply(k + threshold)) {
                n = k;
            } else {
                m = k;
            }
        }

        return k;
    }

    public static double lerp(double delta, double a, double b) {
        return a + delta * (b - a);
    }

    public static double lerp2(double deltaX, double deltaY, double aa, double ab, double ba, double bb) {
        return lerp(deltaY, lerp(deltaX, aa, ab), lerp(deltaX, ba, bb));
    }

    public static double lerp3(double deltaX, double deltaY, double deltaZ, double aaa, double aba, double baa, double bba, double aab, double abb, double bab, double bbb) {
        return lerp(deltaZ, lerp2(deltaX, deltaY, aaa, aba, baa, bba), lerp2(deltaX, deltaY, aab, abb, bab, bbb));
    }


    public Vector3f getBlackbodyColor(double temperature) {
        // Temperature must fit between 1000 and 40000 degrees. I recommend you set the range between 1500 and 15000.
        temperature = MathHelper.clamp(temperature, 1000, 40000);
        temperature /= 100;

        float red, green, blue;

        // Red
        if (temperature <= 66) red = 255;
        else {
            red = (float) (329.698727446 * (Math.pow(temperature - 60, -0.1332047592)));
            red = MathHelper.clamp(red, 0, 255);
        }

        // Green
        if (temperature <= 66) green = (float) (99.4708025861 * Math.log(temperature) - 161.1195681661);
        else green = (float) (288.1221695283 * (Math.pow(temperature - 60, -0.0755148492)));
        green = MathHelper.clamp(green, 0, 255);

        // Blue
        if (temperature >= 66) blue = 255;
        else if (temperature <= 19) blue = 0;
        else {
            blue = (float) (138.5177312231 * Math.log(temperature - 10) - 305.0447927307);
            blue = MathHelper.clamp(blue, 0, 255);
        }

        return new Vector3f(red, green, blue);
    }

    /*public Vec3 getWorldPos(Entity entity, float partialTick) {
        PoseStack matrixStack = new PoseStack();
        Vec3 position = entity.getPosition(partialTick);
        //float dx = (float) (entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * partialTick);
        //float dy = (float) (entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * partialTick);
        //float dz = (float) (entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * partialTick);
        matrixStack.translate(position.x(), position.y(), position.z());
        float dYaw = entity.getViewYRot(partialTick);
        matrixStack.mulPose(new Quaternion(0, -dYaw + 180, 0, true));
        matrixStack.scale(-1, -1, 1);
        matrixStack.translate(0, -1.5f, 0);
        MowzieRenderUtils.matrixStackFromModel(matrixStack, this);
        PoseStack.Pose matrixEntry = matrixStack.last();
        Matrix4f matrix4f = matrixEntry.pose();

        Vector4f vec = new Vector4f(0, 0, 0, 1);
        vec.transform(matrix4f);
        return new Vector3d(vec.getX(), vec.getY(), vec.getZ());
    }*/
}