package BB3.Models;

import com.sun.istack.internal.NotNull;

import java.util.*;

/**
 * Created by kerata on 06/03/16.
 */
public class Tree {

    private Node root;
    private Map<String, ArrayList<Node>> nodes;
    private boolean hasMerged = false;

    public Tree() {
        nodes = new HashMap<>();
    }

    public Map<String, ArrayList<Node>> getNodes() {
        return nodes;
    }

    public Tree(@NotNull Term rootData) {
        this.root = new Node(rootData);
        nodes = new HashMap<>();
        addToNodes(root);
    }

    public void merge(ArrayList<Node> parentPosting, Tree other) {
        parentPosting.replaceAll(attachFrom -> {
            Node newRoot = other.getRoot().generateCopy();
            newRoot.bringAllNodes(new ArrayList<>()).forEach(node -> {
                ArrayList<Node> ownedNodes = nodes.get(node.data.getId());
                if (ownedNodes != null)
                    ownedNodes.add(node);
                else {
                    ArrayList<Node> posting = new ArrayList<>();
                    posting.add(node);
                    nodes.put(node.data.getId(), posting);
                }
            });
            attachFrom.addChild(newRoot);
            return attachFrom;
        });
    }

    public ArrayList<Node> addToNodes(Node node) {
        ArrayList<Node> posting = nodes.get(node.data.getId());
        if (posting == null) {
            posting = new ArrayList<Node>() {
                public boolean add(Node mt) {
                    int index = Collections.binarySearch(this, mt);
                    if (index < 0) index = ~index;
                    super.add(index, mt);
                    return true;
                }
            };
            nodes.put(node.data.getId(), posting);
        }
        posting.add(node);
        return posting;
    }

    public ArrayList<Node> setParentNode(@NotNull Term childData, @NotNull Term parentData) {
        ArrayList<Node> parentNodePosting = getNode(parentData, true);
        ArrayList<Node> childNodePosting = getNode(childData, true);

        for (Node parentNode: parentNodePosting)
            for (Node childNode: childNodePosting) {
                Node newNode = childNode.setParent(parentNode);
                if (newNode.level == 0)
                    root = newNode;
            }

        return parentNodePosting;
    }

    public ArrayList<Node> addChild(@NotNull Term parentData, @NotNull Term childData) {
        ArrayList<Node> parentNodePosting = getNode(parentData, true);
        ArrayList<Node> childNodePosting = getNode(childData, true);

        for (Node parentNode: parentNodePosting)
            for (Node childNode: childNodePosting) {
                parentNode.addChild(childNode);
            }

        return childNodePosting;
    }

    public Node constructFromLeaf(Ontology ontology, Map<String, Term> terms, Term rootData) {
        this.root = new Node(rootData);
        addToNodes(root);
        this.root.buildTree(this, ontology, terms);
        return this.root;
    }

    public boolean hasMerged() {
        return hasMerged;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        if (this.root != null)
            nodes.remove(this.root.data.getId());
        this.root = root;
        addToNodes(root);
    }

    public ArrayList<Node> getNode(String id) {
        return nodes.get(id);
    }

    public ArrayList<Node> getNode(@NotNull Term data, boolean shouldCreateIfNotContains) {
        ArrayList<Node> result = getNode(data.getId());
        if (shouldCreateIfNotContains && result == null) {
            result = addToNodes(new Node(data));
        }
        return result;
    }

    public ArrayList<Node> getNode(@NotNull Term data) {
        return getNode(data, false);
    }

    public ArrayList<Node> findCommonRoots(String[] ids) {
        ArrayList<Node> commonRoots = new ArrayList<>();
        for (int i = 0;i < ids.length - 1;i++) {
            ArrayList<Node> lhs = getNode(ids[i]);
            for (int j = i + 1;j < ids.length;j++) {
                ArrayList<Node> rhs = getNode(ids[j]);
                for (Node mLhs: lhs) {
                    for (Node mRhs : rhs) {
                        Node commonRoot = mLhs.findFirstCommonRoot(mRhs);
                        System.out.println(String.format("Roots of %s - %s: %s",
                                                        mLhs.getData().getId(),
                                                        mRhs.getData().getId(),
                                                        commonRoot.getData().getId()));
                    }
                }
            }
        }
        return commonRoots;
    }

    public void printByLevel() {
        ArrayList<Node> sortedByLevel = new ArrayList<>();
        nodes.values().forEach(sortedByLevel::addAll);
        sortedByLevel.sort((o1, o2) -> {
            int diff = o1.level - o2.level;
            return diff < 0 ? -1 :
                    diff == 0 ? 0 : 1;
        });
        StringBuilder accumulator = new StringBuilder();
        sortedByLevel.forEach(node -> {
            for (int i = 0;i < node.level;i++)
                accumulator
                        .append(" ")
                        .append(node.level)
                        .append(node.data.toString())
                        .append("\n");
        });
        System.out.println(accumulator.toString());
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public static class Node implements Comparable<Node> {

        int level = 0;
        Term data;
        Node parent;
        ArrayList<Node> children;

        public Node(Term data) {
            this.data = data;
            this.children = new ArrayList<>();
            parent = this;
        }

        public Node(Node copy) {
            this.data = copy.data;
            this.children = copy.children;
            this.parent = copy.parent;
        }

        public int getLevel() {
            return level;
        }

        public Term getData() {
            return data;
        }

        public Node getParent() {
            return parent;
        }

        public ArrayList<Node> getChildren() {
            return children;
        }

        public Node setParent(Term parentData) {
            return setParent(new Node(parentData));
        }

        public Node setParent(Node parentNode) {
            parent = parentNode;
            parent.addChild(this);
            return parent;
        }

        public Node addChild(Term childData) {
            return addChild(new Node(childData));
        }

        public Node addChild(Node childNode) {
            childNode.setLevel(level + 1);
            childNode.parent = this;
            children.add(childNode);
            return childNode;
        }

        public void setLevel(int level) {
            this.level = level;
            children.forEach(child -> {
                if (child != null)
                    child.setLevel(level + 1);
            });
        }

        public Node generateCopy() {
            Node copyNode = new Node(data);
            for (Node child: children)
                child.generateCopy().setParent(copyNode);
            return copyNode;
        }

        public ArrayList<Node> bringAllNodes(ArrayList<Node> nodes) {
            nodes.add(this);
            for (Node child: children)
                child.bringAllNodes(nodes);
            return nodes;
        }

        public void buildTree(Tree holder, Ontology ontology, Map<String, Term> terms) {
            data.getIs_a().forEach(relation -> {
                Term parentTerm = terms.get(relation.getTermId());
                if (parentTerm == null) {
                    ontology.getDependencyTrees().forEach(tree -> {
                        ArrayList<Node> parentPosting = tree.getNode(relation.getTermId());
                        if (parentPosting != null) {
                            tree.merge(parentPosting, holder);
                            holder.hasMerged = true;
                        }
                    });
                }
                else {
                    ArrayList<Node> parentPosting = holder.setParentNode(this.data, parentTerm);
                    terms.remove(parentTerm.getId());

                    for (Node node: parentPosting)
                        if (node.data.equals(parentTerm)) {
                            node.buildTree(holder, ontology, terms);
                            break;
                        }
                }
            });
        }

        public Node findFirstCommonRoot(Node other) {
            // If they are same
            if (this.equals(other))
                return this;
            else {
                Node lhs = this.level > other.level ? this : other;
                Node rhs = this.level > other.level ? other : this;
                while (lhs.level != rhs.level)
                    lhs = lhs.parent;

                while (!lhs.equals(rhs)) {
                    lhs = lhs.parent;
                    rhs = rhs.parent;
                }

                return lhs;
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
                accumulator.append("-");
            accumulator
//                    .append(level)
//                    .append(data.toString())
                    .append(data.getId());
            int maxlvl = 7;
            if (level < maxlvl) {
                if (children.size() == 0)
                    accumulator
                            .append("\n");
                else {
                    accumulator
                            .append("-")
                            .append("\n");
                    for (Node child: children)
                        accumulator.append(child.toString());
                }
            }
            else if (level == maxlvl)
                accumulator
                        .append("+")
                        .append("\n");
            return accumulator.toString();
        }

        @Override
        public int compareTo(Node o) {
            int diff = level - o.level;
            return diff < 0 ? -1 :
                    diff == 0 ? 0 : 1;
        }
    }
}