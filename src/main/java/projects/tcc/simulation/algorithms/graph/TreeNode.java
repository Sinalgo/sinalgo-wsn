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
}
