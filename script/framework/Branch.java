package script.framework;

import script.utilities.API;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class Branch extends Leaf {

    public final List<Leaf> children;

    public Branch() {
        this.children = new LinkedList<>();
    }


    public Branch addLeafs(Leaf... leaves) {
        Collections.addAll(this.children, leaves);
        return this;
    }


    @Override
    public int onLoop() {
        return children.stream()
                .filter(c -> Objects.nonNull(c) && c.isValid())
                .findFirst()
                .map(tLeaf -> tLeaf.onLoop()).orElse(600);
    }
}
