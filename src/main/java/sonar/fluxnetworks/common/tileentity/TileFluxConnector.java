package sonar.fluxnetworks.common.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import sonar.fluxnetworks.api.network.ITransferHandler;
import sonar.fluxnetworks.common.block.FluxConnectorBlock;
import sonar.fluxnetworks.common.connection.handler.ConnectionTransferHandler;
import sonar.fluxnetworks.common.handler.TileEntityHandler;
import net.minecraft.tileentity.TileEntity;
import sonar.fluxnetworks.common.tileentity.energy.TileDefaultEnergy;

public abstract class TileFluxConnector extends TileDefaultEnergy {

    public final ConnectionTransferHandler handler = new ConnectionTransferHandler(this, this);

    public TileFluxConnector(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public long addPhantomEnergyToNetwork(Direction dir, long amount, boolean simulate) {
        return isActive() && getConnectionType().canAddEnergy() ? handler.addPhantomEnergyToNetwork(amount, dir, simulate) : 0;
    }

    @Override
    public void updateTransfers(Direction... dirs) {
        super.updateTransfers(dirs);
        boolean sendUpdate = false;
        for(Direction facing : dirs) {
            TileEntity tile = world.getTileEntity(pos.offset(facing));
            boolean b = connections[facing.getIndex()] != 0;
            boolean c = TileEntityHandler.canRenderConnection(tile, facing.getOpposite());
            if(b != c) {
                connections[facing.getIndex()] = (byte) (c ? 1 : 0);
                sendUpdate = true;
            }
        }
        if(sendUpdate) {
            sendPackets();
        }
    }

    @Override
    public void sendPackets() {
        if(!world.isRemote){
            BlockState newState = FluxConnectorBlock.getConnectedState(getBlockState(), getWorld(), getPos());
            world.setBlockState(pos, newState, 3);
            world.markBlockRangeForRenderUpdate(pos, getBlockState(), newState);
            world.notifyBlockUpdate(pos, getBlockState(), newState, 3);
        }
    }



    @Override
    public ITransferHandler getTransferHandler() {
        return handler;
    }

    /* TODO - FIX ONE PROBE "IBIGPOWER"
    @Override
    @Optional.Method(modid = "theoneprobe")
    public long getStoredPower(){
        return getBuffer();
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public long getCapacity(){
        return getBuffer();
    }
    */
}
