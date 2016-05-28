package BB3.Models;

/**
 * Created by hakansahin on 28/05/16.
 */
public class Stat {

    public int truePostiveCnt, falsePositiveCnt, falseNegativeCnt, trialCnt, N = 0;
    public Stat(){
        this.truePostiveCnt = 0;
        this.falsePositiveCnt = 0;
        this.falseNegativeCnt = 0;
        this.trialCnt = 0;
    }

    public void addStats(Stat stat){
        this.truePostiveCnt += stat.truePostiveCnt;
        this.falsePositiveCnt += stat.falsePositiveCnt;
        this.falseNegativeCnt += stat.falseNegativeCnt;
        this.trialCnt += stat.trialCnt;
    }

    @Override
    public String toString() {

        return new StringBuilder().append("True Positive : ").append(this.truePostiveCnt).append("\n")
                .append("False Positive : ").append(this.falsePositiveCnt).append("\n")
                .append("False Negative : ").append(this.falseNegativeCnt).append("\n").toString();
    }
}
