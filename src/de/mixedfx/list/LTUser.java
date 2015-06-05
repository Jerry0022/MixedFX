package de.mixedfx.list;

public class LTUser implements SessionInterface
{
	int	i;

	LTUser(final int i)
	{
		this.i = i;
	}

	@Override
	public Integer getIdentifier()
	{
		return this.i;
	}

	@Override
	public void on()
	{
		System.out.println(this.i + " ist an!");
	}

	@Override
	public void off()
	{
		System.out.println(this.i + " ist aus!");
	}

}
