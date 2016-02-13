package nars.perception.lowlevel.spinglass;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Propagates a ignition chain over all dots which didn't jet got ignited
 */
public class Avalanche {

    static private class NextIgnitionCandidate {
        int index = -1;
        ArrayRealVector bestDirectionOfDotNormalized;
    }

    public double neightborSearchRadius = 0.0f;
    public double minimalDotResultForIgnition;
    public double minimalStrengthForIgnition = 0.0f;

    public int searchRandomIgnitionCandidate(List<SpatialDot> spatialDots, Random random) {
        int tries = 50;

        for( int tryI = 0; tryI < tries; tryI++ ) {
            int candidateIndex = random.nextInt(spatialDots.size());
            SpatialDot candidateDot = spatialDots.get(candidateIndex);

            if( !candidateDot.wasIgnited && candidateDot.spinAttributes.get(0).direction.getNorm() > minimalStrengthForIgnition ) {
                return candidateIndex;
            }
        }

        return -1;
    }

    public List<SpatialDot> ignite(List<SpatialDot> spatialDots, int indexToIgnite) {
        int ignitionIndexForward = indexToIgnite;
        int ignitionIndexBackward = indexToIgnite;
        ArrayRealVector ignitionForwardDirectionNormalized = spatialDots.get(ignitionIndexForward).spinAttributes.get(0).calcNormalizedDirection();
        ArrayRealVector ignitionBackwardDirectionNormalized = new ArrayRealVector(new double[]{0.0,0.0}).subtract(spatialDots.get(ignitionIndexForward).spinAttributes.get(0).calcNormalizedDirection());

        List<SpatialDot> result = new ArrayList<>();

        while( ignitionIndexForward != -1 || ignitionIndexBackward != -1 ) {
            // mark
            if( ignitionIndexForward != -1 ) {
                spatialDots.get(ignitionIndexForward).wasIgnited = true;
            }

            if( ignitionIndexBackward != -1 ) {
                spatialDots.get(ignitionIndexBackward).wasIgnited = true;
            }


            // propagate
            if( ignitionIndexForward != -1 ) {
                NextIgnitionCandidate nextIgnitionCandidateForward = searchBest(spatialDots, ignitionIndexForward, ignitionForwardDirectionNormalized);

                if( nextIgnitionCandidateForward.index != -1 ) {
                    result.add(spatialDots.get(nextIgnitionCandidateForward.index));
                }
                ignitionIndexForward = nextIgnitionCandidateForward.index;
                ignitionForwardDirectionNormalized = nextIgnitionCandidateForward.bestDirectionOfDotNormalized;
            }

            if( ignitionIndexBackward != -1 ) {
                NextIgnitionCandidate nextIgnitionCandidateBackward = searchBest(spatialDots, ignitionIndexBackward, ignitionBackwardDirectionNormalized);

                if( nextIgnitionCandidateBackward.index != -1 ) {
                    result.add(spatialDots.get(nextIgnitionCandidateBackward.index));
                }
                ignitionIndexBackward = nextIgnitionCandidateBackward.index;
                ignitionBackwardDirectionNormalized = nextIgnitionCandidateBackward.bestDirectionOfDotNormalized;
            }
        }

        return result;
    }

    private NextIgnitionCandidate searchBest(List<SpatialDot> spatialDots, int sourceIndex, ArrayRealVector ignitionDirectionNormalized) {
        NextIgnitionCandidate result = new NextIgnitionCandidate();

        ArrayRealVector spatialPosition = spatialDots.get(sourceIndex).spatialPosition;

        int bestCandidateIndex = -1;
        ArrayRealVector bestCandidateDirectionNormalized = null;
        double bestDotProductOfDirection = minimalDotResultForIgnition;

        // TODO< use precalculated neightborlist of dot >
        for( int i = 0; i < spatialDots.size(); i++ ) {
            if( i == sourceIndex ) {
                continue;
            }

            if( spatialDots.get(i).wasIgnited ) {
                continue;
            }

            boolean isMinimalStrengthCriteriaSatisifed = spatialDots.get(i).spinAttributes.get(0).direction.getNorm() > minimalStrengthForIgnition;
            if( !isMinimalStrengthCriteriaSatisifed ) {
                continue;
            }

            double distance = spatialDots.get(i).spatialPosition.subtract(spatialPosition).getNorm();
            if( distance > neightborSearchRadius ) {
                continue;
            }

            ArrayRealVector directionNormalized = spatialDots.get(i).spinAttributes.get(0).calcNormalizedDirection();
            ArrayRealVector antiDirectionNormalized = new ArrayRealVector(new double[]{0.0,0.0}).subtract(directionNormalized);

            double dotResultOfDirection = ignitionDirectionNormalized.dotProduct(directionNormalized);
            double dotResultOfAntiDirection = ignitionDirectionNormalized.dotProduct(antiDirectionNormalized);


            if( dotResultOfDirection > bestDotProductOfDirection ) {
                bestDotProductOfDirection = dotResultOfDirection;
                bestCandidateDirectionNormalized = directionNormalized;
                bestCandidateIndex = i;
            }
            else if( dotResultOfAntiDirection > bestDotProductOfDirection ) {
                bestDotProductOfDirection = dotResultOfAntiDirection;
                bestCandidateDirectionNormalized = antiDirectionNormalized;
                bestCandidateIndex = i;
            }
        }

        result.index = bestCandidateIndex;
        result.bestDirectionOfDotNormalized = bestCandidateDirectionNormalized;

        return result;
    }
}
