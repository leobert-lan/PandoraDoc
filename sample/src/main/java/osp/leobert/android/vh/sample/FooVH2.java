package osp.leobert.android.vh.sample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import osp.leobert.android.pandora.rv.ViewHolderCreator;
import osp.leobert.android.vh.reporter.ViewHolder;


/**
 * <p><b>Package:</b> osp.leobert.android.vh.sample </p>
 * <p><b>Project:</b> PandoraDoc </p>
 * <p><b>Classname:</b> Foo </p>
 * <p><b>Description:</b> TODO </p>
 * Created by leobert on 2019-06-03.
 */
@ViewHolder(usage = ViewHolder.Global, alias = "别名", version = 2,
        pic = {"http://img0.imgtn.bdimg.com/it/u=3560217793,3007674441&fm=26&gp=0.jpg",
                "http://img2.imgtn.bdimg.com/it/u=2006056231,3443127119&fm=26&gp=0.jpg"})
public class FooVH2 extends AbsViewHolder<FooVO2> {
    private final ItemInteract mItemInteract;

    private FooVO2 mData;


    public FooVH2(View itemView, ItemInteract itemInteract) {
        super(itemView);
        this.mItemInteract = itemInteract;
        //TODO: find views and bind actions here
    }

    @Override
    public void setData(FooVO2 data) {
        mData = data;
        //TODO: bind data to views

    }

    public static final class Creator extends ViewHolderCreator {
        private final ItemInteract itemInteract;

        public Creator(ItemInteract itemInteract) {
            this.itemInteract = itemInteract;
        }

        @Override
        public AbsViewHolder<FooVO2> createViewHolder(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.app_vh_foo, parent, false);
            return new FooVH2(view, itemInteract);
        }
    }

    public interface ItemInteract {
        //TODO: define your actions here
    }


}