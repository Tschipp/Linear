package tschipp.linear.common.helper;

public enum BuildMode
{
	
	LINE3(3, false, "line_3_axes"),
	LINE2(2, false, "line_2_axes"),
	LINE1(1, false, "line_1_axis"),
	WALL3(3, true, "wall_3_axes"),
	WALL2(2, true, "wall_2_axes");
	
	private int axis;
	private boolean isPlane;
	private String name;
	
	
	private BuildMode(int axis, boolean isPlane, String name)
	{
		this.axis = axis;
		this.isPlane = isPlane;
		this.name = name;
	}
	
	public int getAxis()
	{
		return axis;
	}

	public boolean isPlane()
	{
		return isPlane;
	}

	public String getName()
	{
		return name;
	}
	
	

	public static BuildMode getByName(String name)
	{
		for(BuildMode mode : values())
		{
			if(mode.getName().equalsIgnoreCase(name))
				return mode;
		}
		return null;
	}
}
