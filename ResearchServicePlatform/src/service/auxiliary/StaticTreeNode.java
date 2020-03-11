package service.auxiliary;

import java.util.ArrayList;
import java.util.List;

public class StaticTreeNode<C> {
	
	private final C content;
	private final StaticTreeNode<C> parent;
	private List<StaticTreeNode<C>> children = new ArrayList<>();
	private boolean visited;
	
	public StaticTreeNode(C content, StaticTreeNode<C> parent) {
		this.content = content;
		this.parent = parent;
		
		if (parent != null) {
			parent.addChild(this);
		}
	}
	
	public boolean isVisited() {
		return visited;
	}
	
	public C getContent() {
		return content;
	}
	
	public StaticTreeNode<C> getParent() {
		return parent;
	}
	
	public void addChild(StaticTreeNode<C> node) throws IllegalArgumentException {
		
		if (node.getParent() != this) {
			throw new IllegalArgumentException("The given node does not have this node as parent!");
		}
		
		children.add(node);
	}
	
	public boolean hasChildWithContent(C givenContent) {	
		return children.stream().anyMatch(x -> x.getContent().equals(givenContent));
	}
	
	public StaticTreeNode<C> findChildWithContent(C givenContent) {
		return children.stream().filter(x -> x.getContent().equals(givenContent)).findFirst().orElse(null);
	}
	
	public StaticTreeNode<C> getNext() {
		
		visited = true;
		
		for (StaticTreeNode<C> node : children) {
			if (!node.isVisited()) {
				return node;
			}
		}
		
		return parent;
	}
	
	public void reset() {
		visited = false;
	}
}
