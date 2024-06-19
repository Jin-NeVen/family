package com.ntt.jin.family.data

import com.ntt.skyway.room.sfu.LocalSFURoomMember
import com.ntt.skyway.room.sfu.SFURoom

data class User(
    val name: String,
    var joinedRoom: SFURoom? = null,
    var localSFURoomMember: LocalSFURoomMember? = null
    )
