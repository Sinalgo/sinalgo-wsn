package projects.tcc.simulation.algorithms.graph;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(of = "value")
@Getter
@Setter
@RequiredArgsConstructor
public class TreeNode<T> {
    private TreeNode<T> parent;
    private final T value;
    private final List<TreeNode<T>> children = new ArrayList<>();

    public void print() {
        System.out.println("START printing resulting tree");
        this.print(0);
        System.out.println("END printing resulting tree");
    }

    private void print(int tabulation) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tabulation; i++) {
            sb.append("\t");
        }
        sb.append(this.getValue());
        System.out.println(sb.toString());
        for (TreeNode<T> child : this.getChildren()) {
            child.print(tabulation + 1);
        }
    }
}
