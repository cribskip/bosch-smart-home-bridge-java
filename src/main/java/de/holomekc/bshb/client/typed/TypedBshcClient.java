package de.holomekc.bshb.client.typed;

import java.util.List;

import de.holomekc.bshb.client.BshcClient;
import de.holomekc.bshb.model.device.Device;
import de.holomekc.bshb.model.information.Information;
import de.holomekc.bshb.model.information.PublicInformation;
import de.holomekc.bshb.model.room.Room;
import de.holomekc.bshb.model.service.Service;
import de.holomekc.bshb.model.service.State;
import io.reactivex.Observable;

/**
 * @author Christopher Holomek
 * @since 18.01.2020
 */
public class TypedBshcClient {

    private final BshcClient client;

    public TypedBshcClient(final BshcClient client) {
        this.client = client;
    }

    public Observable<TypedBshbResponse<Information>> getInformation() {
        return this.client.getInformation().flatMap(bshbResponse -> Observable
                .just(new TypedBshbResponse<>(bshbResponse, bshbResponse.readEntity(Information.class))));
    }

    /**
     * Get information about BSHC
     *
     * @return bshb response object
     */
    public Observable<TypedBshbResponse<PublicInformation>> getPublicInformation() {
        return this.client.getPublicInformation().flatMap(bshbResponse -> Observable
                .just(new TypedBshbResponse<>(bshbResponse, bshbResponse.readEntity(PublicInformation.class))));
    }

    /**
     * Get all rooms stored
     *
     * @return bshb response object
     */
    public Observable<TypedBshbResponse<List<Room>>> getRooms() {
        return this.client.getRooms().flatMap(bshbResponse -> Observable
                .just(new TypedBshbResponse<>(bshbResponse, bshbResponse.readList(Room.class))));
    }

    public Observable<TypedBshbResponse<List<Device>>> getDevices() {
        return this.client.getDevices().flatMap(bshbResponse -> Observable
                .just(new TypedBshbResponse<>(bshbResponse, bshbResponse.readList(Device.class))));
    }

    public Observable<TypedBshbResponse<Device>> getDevice(final String id) {
        return this.client.getDevice(id).flatMap(bshbResponse -> Observable
                .just(new TypedBshbResponse<>(bshbResponse, bshbResponse.readEntity(Device.class))));
    }

    @SuppressWarnings("unchecked")
    public Observable<TypedBshbResponse<List<Service<? extends State>>>> getDevicesServices() {
        return this.client.getDevicesServices().flatMap(bshbResponse -> Observable
                .just(new TypedBshbResponse<>(bshbResponse,
                        bshbResponse.readList((Class<Service<?>>) (Class<?>) Service.class))));
    }

    @SuppressWarnings("unchecked")
    public Observable<TypedBshbResponse<List<Service<? extends State>>>> getDeviceServices(final String deviceId) {
        return this.client.getDeviceServices(deviceId).flatMap(bshbResponse -> Observable
                .just(new TypedBshbResponse<>(bshbResponse,
                        bshbResponse.readList((Class<Service<?>>) (Class<?>) Service.class))));
    }

    @SuppressWarnings("unchecked")
    public <T extends State> Observable<TypedBshbResponse<Service<T>>> getDeviceServices(final String deviceId,
            final String serviceId) {
        return this.client.getDeviceServices(deviceId, serviceId).flatMap(bshbResponse -> Observable
                .just(new TypedBshbResponse<>(bshbResponse,
                        bshbResponse.readEntity((Class<Service<T>>) (Class<?>) Service.class))));
    }
}
