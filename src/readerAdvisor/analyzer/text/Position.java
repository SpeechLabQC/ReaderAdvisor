package readerAdvisor.analyzer.text;

/**
 * Denotes the start and end position of a word
 */
public class Position {
    private int startPosition;
    private int endPosition;

    public Position(int startPosition, int endPosition){
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public void setStartPosition(int startPosition){
        this.startPosition = startPosition;
    }

    public void setEndPosition(int endPosition){
        this.endPosition = endPosition;
    }

    public int getStartPosition(){
        return startPosition;
    }

    public int getEndPosition(){
        return endPosition;
    }

    public void increaseStartPositionBy(int offset){
        this.startPosition += offset;
    }

    public int getLength(){
        return endPosition-startPosition;
    }
}
