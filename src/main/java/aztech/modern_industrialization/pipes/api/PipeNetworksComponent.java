package aztech.modern_industrialization.pipes.api;

import nerdhub.cardinal.components.api.component.Component;

public interface PipeNetworksComponent extends Component {
    PipeNetworkManager getManager(PipeNetworkType type);
    void onServerTickStart();
}
