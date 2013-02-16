package org.yogpstop.qp;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerDummy extends Container {
	int xCoord, yCoord, zCoord;

	public ContainerDummy(int x, int y, int z) {
		xCoord = x;
		yCoord = y;
		zCoord = z;
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return var1.getDistanceSq((double) this.xCoord + 0.5D,
				(double) this.yCoord + 0.5D, (double) this.zCoord + 0.5D) <= 64.0D;
	}
}
