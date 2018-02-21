public class BlockList {
    public String getMyip() {
        return myip;
    }

    public void setMyip(String myip) {
        this.myip = myip;
    }

    public String getBlockip() {
        return blockip;
    }

    public void setBlockip(String blockip) {
        this.blockip = blockip;
    }

    String myip;
    String blockip;
    BlockList(String myip,String blockip){
        this.myip = myip;
        this.blockip = blockip;
    }


    public  int checkBlock(String compareMyip,String compareBlockip){
        if(this.myip.equals(compareMyip) &&  this.blockip.equals(compareBlockip)){
            return 1;
        }

        if(this.myip.equals(compareBlockip) &&  this.blockip.equals(compareMyip)){
            return 1;
        }


        return 0;

    }
}
