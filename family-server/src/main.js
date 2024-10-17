import {
  nowInSec,
  SkyWayAuthToken,
  SkyWayContext,
  SkyWayRoom,
  SkyWayStreamFactory,
  uuidV4,
} from '@skyway-sdk/room';


const token = new SkyWayAuthToken({
  jti: uuidV4(),
  iat: nowInSec(),
  exp: nowInSec() + 60 * 60 * 24,
  scope: {
    app: {
      id: 'b72ba745-4f87-48e5-a823-fa3107440350',
      turn: true,
      actions: ['read'],
      channels: [
        {
          id: '*',
          name: '*',
          actions: ['write'],
          members: [
            {
              id: '*',
              name: '*',
              actions: ['write'],
              publication: {
                actions: ['write'],
              },
              subscription: {
                actions: ['write'],
              },
            },
          ],

          sfuBots: [
            {
              actions: ['write'],
              forwardings: [
                {
                  actions: ['write'],
                },
              ],
            },
          ],
        },
      ],
    },
  },
}).encode('q8RiV/151sRx/H2tTfkTIA2E51Dy1mrBWhqxePzsAKg=');

(async () => {
  const localVideo = document.getElementById('local-video');
  const buttonArea = document.getElementById('button-area');
  const remoteMediaArea = document.getElementById('remote-media-area');
  const roomNameInput = document.getElementById('room-name');

  const myId = document.getElementById('my-id');
  const myNameInput = document.getElementById('my-name')
  const myToken = document.getElementById('my-token');
  const joinButton = document.getElementById('join');
  const closeButton = document.getElementById('close');
  const disposeButton = document.getElementById('dispose');
  const video = await SkyWayStreamFactory.createCameraVideoStream();
  video.attach(localVideo);
  await localVideo.play();

  let room;
  joinButton.onclick = async () => {
    if (roomNameInput.value === '') return;
    myToken.textContent = token;
    const context = await SkyWayContext.Create(token);
    room = await SkyWayRoom.FindOrCreate(context, {
      type: 'sfu',
      name: roomNameInput.value,
    });
    const me = await room.join({
      // RoomNameとMemberNameは同じにする
      name: myNameInput.value
    });

    myId.textContent = me.id;

    // await me.publish(audio);
    await me.publish(video);

    const subscribeAndAttach = (publication) => {
      if (publication.publisher.id === me.id) return;

      const subscribeButton = document.createElement('button');
      subscribeButton.textContent = `${publication.publisher.id}: ${publication.contentType}`;
      buttonArea.appendChild(subscribeButton);

      subscribeButton.onclick = async () => {
        const { stream } = await me.subscribe(publication.id);

        let newMedia;
        switch (stream.track.kind) {
          case 'video':
            newMedia = document.createElement('video');
            newMedia.playsInline = true;
            newMedia.autoplay = true;
            break;
          case 'audio':
            newMedia = document.createElement('audio');
            newMedia.controls = true;
            newMedia.autoplay = true;
            break;
          default:
            return;
        }

        stream.attach(newMedia);
        remoteMediaArea.appendChild(newMedia);
      };
    };

    room.publications.forEach(subscribeAndAttach);
    room.onStreamPublished.add((e) => subscribeAndAttach(e.publication));
  };
  closeButton.onclick = async () => {
    room.close();
  }
  disposeButton.onclick = async () => {
    room.dispose();
  }
})();
