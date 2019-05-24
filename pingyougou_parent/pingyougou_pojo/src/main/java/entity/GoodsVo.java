package entity;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;

import java.io.Serializable;
import java.util.List;

/**
 * 商品包装类
 * 必须实现序列化接口，操作时忘了~~~~~~
 */
public class GoodsVo implements Serializable{

    //商品表
    private Goods goods;
    //商品详情表
    private GoodsDesc goodsDesc;
    //库存表（商品规格表）
    private List<Item> itemList;

    public GoodsVo() {
    }

    public GoodsVo(Goods goods, GoodsDesc goodsDesc, List<Item> itemList) {
        this.goods = goods;
        this.goodsDesc = goodsDesc;
        this.itemList = itemList;
    }

    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
    }

    public GoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(GoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    @Override
    public String toString() {
        return "GoodsVo{" +
                "goods=" + goods +
                ", goodsDesc=" + goodsDesc +
                ", itemList=" + itemList +
                '}';
    }
}
