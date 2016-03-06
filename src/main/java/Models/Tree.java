package Models;

import com.sun.istack.internal.NotNull;

import java.util.*;

/**
 * Created by kerata on 06/03/16.
 */
public class Tree {

    private Node root;
    private Map<String, Node> nodes;

    public Tree() {
        nodes = new HashMap<>();
    }

    public Tree(@NotNull Term rootData) {
        this.root = new Node(rootData);
        nodes = new HashMap<>();
        nodes.put(rootData.getId(), root);
    }

    public Node setParentNode(@NotNull Term childData, @NotNull Term parentData) {
        Node parentNode = getNode(parentData, true);
        Node childNode = getNode(childData, true);

        Node newNode = childNode.setParent(parentNode);
        if (newNode.level == 0)
            root = newNode;

        return newNode;
    }

    public Node addChild(@NotNull Term parentData, @NotNull Term childData) {
        Node parentNode = getNode(parentData, true);
        Node childNode = getNode(childData, true);

        return parentNode.addChild(childNode);
    }

    public Node constructFromLeaf(Map<String, Term> terms, Term rootData) {
        this.root = new Node(rootData);
        this.root.findRoot(this, terms);
        return this.root;
    }

    public Node getRoot() {
        return root;
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public Node getNode(@NotNull Term data, boolean shouldCreateIfNotContains) {
        Node result = getNode(data.getId());
        if (shouldCreateIfNotContains && result == null) {
            result = new Node(data);
            nodes.put(data.getId(), result);
        }
        return result;
    }

    public Node getNode(@NotNull Term data) {
        return getNode(data, false);
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public static class Node {

        int level = 0;
        Term data;
        Node parent;
        ArrayList<Node> children;

        public Node(Term data) {
            this.data = data;
            this.children = new ArrayList<>();
            parent = this;
        }

        public Node setParent(Term parentData) {
            return setParent(new Node(parentData));
        }

        public Node setParent(Node parentNode) {
            parent = parentNode;
            parent.addChild(this);
            parent.setLevel(level - 1);
            return parent;
        }

        public Node addChild(Term childData) {
            return addChild(new Node(childData));
        }

        public Node addChild(Node childNode) {
            childNode.setLevel(level + 1);
            children.add(childNode);
            return childNode;
        }

        public void setLevel(int level) {
            this.level = level;
            for (Node child: children)
                child.setLevel(level + 1);
        }

        public void findRoot(Tree holder, Map<String, Term> terms) {
            Iterator<String> iterator = data.getIs_a().iterator();
            while (iterator.hasNext()) {
                String parentId = iterator.next();
                Term parentTerm = terms.get(parentId);
                if (parentTerm == null) {
                    // TODO reach trees in Ontology and merge, not removing and merging causes many duplicates...
//                    System.out.println(String.format("No term exists with id: %s", childId));
                    continue;
                }
                Node parent = holder.setParentNode(this.data, parentTerm);
                parent.findRoot(holder, terms);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Node) {
                Node other = (Node) obj;
                return other.data.getId().equals(data.getId());
            }
            else return false;
        }

        @Override
        public String toString() {
            StringBuilder accumulator = new StringBuilder();
            for (int i = 0;i < level;i++)
                accumulator.append(" ");
            accumulator.append(level).append(data.toString()).append("\n");

            for (Node child: children)
                accumulator.append(child.toString());
            return accumulator.toString();
        }
    }
}