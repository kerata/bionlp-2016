package BB3.Models;

/**
 * Created by hakansahin on 28/05/16.
 */
public class Stat {

    public int
            truePostiveCnt,
            falsePositiveCnt,
            falseNegativeCnt;
    public double
            precision,
            recall,
            fScore;
    public boolean
            isStatisticsComputed;

    public Stat(){
        this.truePostiveCnt = 0;
        this.falsePositiveCnt = 0;
        this.falseNegativeCnt = 0;
        this.precision = 0;
        this.recall = 0;
        this.fScore = 0;
        this.isStatisticsComputed = false;
    }

    public void addStats(Stat stat){
        this.truePostiveCnt += stat.truePostiveCnt;
        this.falsePositiveCnt += stat.falsePositiveCnt;
        this.falseNegativeCnt += stat.falseNegativeCnt;
    }

    public void computeStatistics(){
        this.precision  = (this.truePostiveCnt + this.falsePositiveCnt) == 0 ? 0 : (double) this.truePostiveCnt / (this.truePostiveCnt + this.falsePositiveCnt);
        this.recall     = (this.truePostiveCnt + this.falsePositiveCnt) == 0 ? 0 : (double) this.truePostiveCnt / (this.truePostiveCnt + this.falseNegativeCnt);
        this.fScore     = (this.precision + this.recall) == 0 ? 0 : 2 * this.precision * this.recall / (this.precision + this.recall);
        this.isStatisticsComputed = true;
    }

    @Override
    public String toString() {
        if(!isStatisticsComputed) computeStatistics();
        return String.format("True Positive : %d\nFalse Positive : %d\nFalse Negative : %d\nPrecision : %.2f\nRecall : %.2f\nF-Score : %.2f\n",
                this.truePostiveCnt, this.falsePositiveCnt, this.falseNegativeCnt, this.precision, this.recall, this.fScore);
    }
}
