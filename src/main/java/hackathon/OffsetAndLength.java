package hackathon;

public class OffsetAndLength {
    private final int offset;
    private final int length;

    public OffsetAndLength(int offset, int length) {
        this.offset = offset;
        this.length = length;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "OffsetAndLength{" +
                "offset=" + offset +
                ", length=" + length +
                '}';
    }
}
