package Models;

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

    public void merge(Node attachFrom, Tree other) {
        // TODO how to add all branches, all parents not just one...
        nodes.putAll(other.getNodes());
//        other.nodes.forEach((id, posting) -> {
//            if (nodes.containsKey(id)) {
//                ArrayList<Node> addTo = nodes.get(id);
//                for (int i = 0;i < posting.size();i++)
//                    addTo.add(posting.get(i));
//            }
//            else nodes.put(id, posting);
//        });
        attachFrom.addChild(other.getRoot());
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

    public Node constructFromLeaf(Ontology ontology, Term rootData) {
        this.root = new Node(rootData);
//        this.nodes.put(rootData.getId(), root);
        addToNodes(root);
        this.root.findRoot(this, ontology);
        return this.root;
    }

    public boolean hasMerged() {
        return hasMerged;
    }

    public Node getRoot() {
        return root;
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

    public void printByLevel() {
        ArrayList<Node> sortedByLevel = new ArrayList<>();
        for (ArrayList<Node> posting: nodes.values())
            sortedByLevel.addAll(posting);
        sortedByLevel.sort((o1, o2) -> {
            int diff = o1.level - o2.level;
            return diff < 0 ? -1 :
                    diff == 0 ? 0 : 1;
        });
        StringBuilder accumulator = new StringBuilder();
        sortedByLevel.forEach(node -> {
            for (int i = 0;i < node.level;i++)
                accumulator.append(" ");
            accumulator
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
            children.add(childNode);
            return childNode;
        }

        public void setLevel(int level) {
            this.level = level;
            Iterator<Node> iterator = children.iterator();
            while (iterator.hasNext()) {
                Node child = iterator.next();
                if (child != null)
                    child.setLevel(level + 1);
            }
        }

        public void findRoot(Tree holder, Ontology ontology) {
            Iterator<String> iterator = data.getIs_a().iterator();
            while (iterator.hasNext()) {
                String parentId = iterator.next();
                Term parentTerm = ontology.getTerms().get(parentId);
                if (parentTerm == null) {
                    for (Iterator<Tree> treeIterator = ontology.getDependencyTrees().iterator(); treeIterator.hasNext();) {
                        Tree tree = treeIterator.next();
                        ArrayList<Node> parentPosting = tree.getNode(parentId);
                        if (parentPosting != null) {
                            for (Node parent: parentPosting) {
                                tree.merge(parent, holder);
                                holder.hasMerged = true;
                                // TODO what to do...should there be repetitions or not?
                                break;
                            }
                        }
                    }
                    continue;
                }
                ArrayList<Node> parentPosting = holder.setParentNode(this.data, parentTerm);
                ontology.getTerms().remove(parentTerm.getId());

                Node parent = null;
                for (Node node: parentPosting)
                    if (node.data.equals(parentTerm)) {
                        parent = node;
                        break;
                    }
                if (parent != null)
                    parent.findRoot(holder, ontology);
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

        @Override
        public int compareTo(Node o) {
            int diff = level - o.level;
            return diff < 0 ? -1 :
                    diff == 0 ? 0 : 1;
        }
    }
}