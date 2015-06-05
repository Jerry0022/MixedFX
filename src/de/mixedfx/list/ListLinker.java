package de.mixedfx.list;

import javafx.beans.property.ListPropertyBase;
import javafx.collections.ListChangeListener;

public class ListLinker
{
	public static void link(final ListPropertyBase<Identifiable> mainList, final ListPropertyBase<SessionInterface> depList)
	{
		synchronized (depList)
		{
			for (final Identifiable mainE : mainList)
			{
				int index;
				if ((index = depList.lastIndexOf(mainE)) != -1)
				{
					depList.get(index).on();
					depList.set(index, depList.get(index));
				}
			}

			mainList.addListener((ListChangeListener<Object>) c ->
			{
				synchronized (depList)
				{
					while (c.next())
					{
						if (c.wasAdded())
						{
							for (final Object o : c.getAddedSubList())
							{
								int index;
								if ((index = depList.lastIndexOf(o)) != -1)
								{
									depList.get(index).on();
									depList.set(index, depList.get(index));
								}
							}
						}
						else
							if (c.wasRemoved())
							{
								for (final Object o : c.getRemoved())
								{
									int index;
									if ((index = depList.lastIndexOf(o)) != -1)
									{
										depList.get(index).off();
										depList.set(index, depList.get(index));
									}
								}
							}
					}
				}
			});
		}
	}
}
