package services.library.drowTools;

public class Node {
    public int x, y;
    String name;
    String collection;
    String parent_id;
    String type;

    public Node(String myName,String collection, int myX, int myY,String parent_id,String type) {
        x = myX;
        y = myY;
        this.parent_id = parent_id;
        name = myName.substring(0, Math.min(myName.length(), 15));
        this.collection = collection;
        this.type = type;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isClicked(int x,int y){
        int limit = 20;
        return x > this.x - limit && x < this.x + limit && y < this.y + limit && y > this.y - limit;
    }
}