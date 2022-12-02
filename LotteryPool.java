import java.util.ArrayList;
import java.util.Date;

public class LotteryPool {
    private Date drawDate;
    private int pricePool;
    private ArrayList<User> users = new ArrayList<User>();

    public LotteryPool(Date drawDate) {
        this.drawDate = drawDate;
        this.pricePool = 0;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void addMoney(int money) {
        pricePool += money;
    }

    public Date getDate() {
        return drawDate;
    }

    public int getPricePool() {
        return pricePool;
    }
}
