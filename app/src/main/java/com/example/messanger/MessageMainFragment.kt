package com.example.messanger

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.messanger.MessageAdapter.Companion.mediaPlayer
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.YourProfileFragment.Companion.phoneNumber
import com.example.messanger.databinding.FragmentMessageMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class MessageMainFragment : Fragment(), TabsFoldersInterface {

    private var _binding: FragmentMessageMainBinding? = null
    val binding get() = _binding!!
    lateinit var headerView: View
    lateinit var btnUserNameAndNumber: Button
    lateinit var imgBtnProfileDrawerLayoutMessageMain: ImageButton
    lateinit var btnUserNameAndNumberAddButton: Button
    lateinit var tvUserNameAndNumber: TextView
    lateinit var auth: FirebaseAuth
    lateinit var ref: DatabaseReference
    var receiverId: String? = ""
    private lateinit var job: Job
    var textSizeOfMessange: Int? = null
    val newMessages = mutableListOf<MessageTypeClass>()
    val messageGroupList = mutableListOf<MessageTypeClassGroup>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessageMainBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        senderIdGet()
        binding.fab.setOnClickListener{
            navController.navigate(R.id.action_messageMainFragment_to_newMessageFragment)
        }

        val builder = AppBarConfiguration.Builder(navController.graph)
        builder.setOpenableLayout(binding.drawer)
        NavigationUI.setupWithNavController(binding.navView, navController)
        headerView = binding.navView.getHeaderView(0)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)

        binding.btnSearchMessage.setOnClickListener {
            navController.navigate(R.id.action_messageMainFragment_to_searchMessageFragment)
        }

        binding.btnRvTabOfMessangesOpenMessageELECT.setOnClickListener {
            val bundle = Bundle().apply {
                putString("TEXTSIZE", textSizeOfMessange.toString())
            }
            navController.navigate(R.id.action_messageMainFragment_to_electsFragment, bundle)
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.yourProfileFragment -> {
                    navController.navigate(R.id.action_messageMainFragment_to_yourProfileFragment)
                    binding.drawer.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.createGroupFragment -> {
                    navController.navigate(R.id.action_messageMainFragment_to_createGroupFragment)
                    binding.drawer.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.newMessageFragment -> {
                    navController.navigate(R.id.action_messageMainFragment_to_newMessageFragment)
                    binding.drawer.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.electsFragment -> {
                    val bundle = Bundle().apply {
                        putString("TEXTSIZE", textSizeOfMessange.toString())
                    }
                    navController.navigate(R.id.action_messageMainFragment_to_electsFragment, bundle)
                    binding.drawer.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.settingsFragment -> {
                    navController.navigate(R.id.action_messageMainFragment_to_settingsFragment)
                    binding.drawer.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.inviteFrensFragment -> {
                    navController.navigate(R.id.action_messageMainFragment_to_inviteFrensFragment)
                    binding.drawer.closeDrawer(GravityCompat.START)
                    true
                }
                // обработка других элементов меню
                else -> false
            }
        }


        binding.btnDrawerOpen.setOnClickListener {
            binding.drawer.openDrawer(GravityCompat.START)
        }

        navController.addOnDestinationChangedListener {_,destination,_ ->
            destination.label = ""
            binding.toolbar.title = destination.label
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Нельзя вернуться назад с этого экрана", Toast.LENGTH_SHORT).show()
        }
        btnUserNameAndNumber = headerView.findViewById(R.id.btnUserNameAndNumber)
        tvUserNameAndNumber = headerView.findViewById(R.id.tvNumberUser)
        btnUserNameAndNumberAddButton = headerView.findViewById(R.id.btnUserNameAndNumberAddButton)
        imgBtnProfileDrawerLayoutMessageMain = headerView.findViewById(R.id.imgBtnProfileDrawerLayoutMessageMain)
        imgBtnProfileDrawerLayoutMessageMain.setOnClickListener {
            navController.navigate(R.id.action_messageMainFragment_to_settingsFragment)
        }
        var drawableRight = 1
        btnUserNameAndNumber.setOnClickListener {
            if(drawableRight == 1) {
                val res = resources.getDrawable(R.drawable.icon_up)
                btnUserNameAndNumber.setCompoundDrawablesWithIntrinsicBounds(null, null, res, null)
                btnUserNameAndNumberAddButton.visibility = View.VISIBLE
                drawableRight = 0
            } else {
                val res = resources.getDrawable(R.drawable.icon_down)
                btnUserNameAndNumber.setCompoundDrawablesWithIntrinsicBounds(null, null, res, null)
                btnUserNameAndNumberAddButton.visibility = View.GONE
                drawableRight = 1
            }
        }

        btnUserNameAndNumberAddButton.setOnClickListener {
            binding.drawer.closeDrawer(GravityCompat.START)
            val res = resources.getDrawable(R.drawable.icon_down)
            btnUserNameAndNumber.setCompoundDrawablesWithIntrinsicBounds(null, null, res, null)
            btnUserNameAndNumberAddButton.visibility = View.GONE
            drawableRight = 1
        }

        val nameTabsFolders = mutableListOf("Все")

        arguments.let { bundle ->
            receiverId = bundle?.getString("ID")
            Log.e("rec", receiverId.toString())
        }

        ref = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val currentNumber = currentUser?.phoneNumber
        var userId = ""
        val userRef = ref.child("user")
        val userReference = ref.child("user")
        if(currentNumber != null) {
            userRef.orderByChild("number").equalTo(phoneNumber)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (userSnapshot in snapshot.children) {
                                userId = userSnapshot.key!!
                                btnUserNameAndNumber.text = userSnapshot.child("name").value.toString()
                                btnUserNameAndNumberAddButton.text = userSnapshot.child("name").value.toString()
                                tvUserNameAndNumber.text = userSnapshot.child("number").value.toString()
                                val refsOfuser = FirebaseDatabase.getInstance().reference
                                refsOfuser.child("user").child(userId).child("folders").addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshots: DataSnapshot) {
                                        for(snapys in snapshots.children) {
                                            nameTabsFolders.add(snapys.child("name").value.toString())
                                        }
                                        val adapterTabsFolders = TabsFoldersAdapter(nameTabsFolders, navController, this@MessageMainFragment)
                                        binding.rvTabsFolders.adapter = adapterTabsFolders
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                    }

                                })
                                val resString = userSnapshot.child("profilePicture").value.toString()
                                Glide.with(imgBtnProfileDrawerLayoutMessageMain.context)
                                    .load(resString)
                                    .into(imgBtnProfileDrawerLayoutMessageMain)
                                job = CoroutineScope(Dispatchers.Main).launch {
                                    var run = 0
                                    while(run != 3 && isActive) {
                                        userReference.child(userId).child("messages").addListenerForSingleValueEvent(object: ValueEventListener{
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                for(childes in snapshot.children) {
                                                    for(childes1 in childes.children) {
                                                        userReference.child(userId).child("textSize").get().addOnSuccessListener { snap->
                                                            textSizeOfMessange = snap.value.toString().toInt()
                                                        }
                                                        val id = childes.key.toString()
                                                        var names: String; var number: String; var image: String
                                                        userReference.child(id).get().addOnSuccessListener { query->
                                                            image = query.child("profilePicture").value.toString()
                                                            names = query.child("name").value.toString()
                                                            number = query.child("number").value.toString()
                                                            if(newMessages.size != 0) {
                                                                val isDuplicate = newMessages.any {
                                                                    it.nameOfChat == names && it.imgAvaOfChatURL == image
                                                                }

                                                                if (!isDuplicate) {
                                                                    newMessages.add(MessageTypeClass(names, image, number, id))
                                                                    Log.e("wtf", newMessages.toString())
                                                                }
                                                            } else {
                                                                newMessages.add(MessageTypeClass(names, image, number, id))
                                                            }
                                                            if(textSizeOfMessange != null) {
                                                                val adapter = TabOfMessangesAdapter(newMessages, navController, textSizeOfMessange!!)
                                                                binding.rvTabOfMessanges.adapter = adapter
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                            }

                                        })
                                        run++
                                        delay(100)
                                    }
                                }


                            }
                        } else {
                            Log.e("userId", "Пользователь не найден")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("DatabaseError", error.message)
                    }
                })
        }
        return  view
    }

    fun senderIdGet() {
        val navController = findNavController()
        val database = FirebaseDatabase.getInstance().reference
        val groupRef = database.child("group")
        val userReference2 = database.child("user")

        groupRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    for (userSnap in snap.child("users").children) {
                        val userId = userSnap.child("userId").value.toString()

                        if (userId == currentUserId.toString()) {
                            userReference2.child(currentUserId.toString()).child("textSize").get().addOnSuccessListener { snap->
                                textSizeOfMessange = snap.value.toString().toInt()
                                Log.e("USERIDD",textSizeOfMessange.toString())
                            }
                            val nameGroup: String = snap.child("name").value.toString()
                            val imgGroup: String = snap.child("profilePicture").value.toString()
                            if(messageGroupList.size != 0) {
                                for(i in messageGroupList.indices) {
                                    if((imgGroup != "" || nameGroup != "") && (!messageGroupList[i].nameOfChat.contains(nameGroup) || !messageGroupList[i].imgAvaOfChatURL.contains(imgGroup))) {
                                        messageGroupList.add(MessageTypeClassGroup(nameGroup, imgGroup))
                                    }
                                }
                            } else {
                                messageGroupList.add(MessageTypeClassGroup(nameGroup, imgGroup))
                            }
                            break
                        }
                    }
                }
                job = CoroutineScope(Dispatchers.Main).launch {
                    var run = 0
                    while (run !=3 && isActive) {
                        if(textSizeOfMessange != null) {
                            val adapter = TabOfMessangesGroupAdapter(messageGroupList, navController, textSizeOfMessange!!)
                            binding.rvTabOfMessangesGroup.adapter = adapter
                        }
                        run++
                        delay(200)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onButtonClick(string: String) {
        val usrefs = FirebaseDatabase.getInstance().reference
        val navController = findNavController()
        usrefs.child("user").child(currentUserId.toString()).child("folders").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                newMessages.clear()
                messageGroupList.clear()
                for(snap in snapshot.children) {
                    if(snap.child("name").value.toString() == string) {
                        if(snap.child("number").value.toString() != "null" ) {
                            for(snap1 in snap.child("listsUser").children) {
                                val nameOfUser = snap1.child("nameOfChat").value.toString()
                                val imgAvaOfChatURL = snap1.child("imgAvaOfChatURL").value.toString()
                                val numberOfUser = snap1.child("number").value.toString()
                                val id = snap1.child("id").value.toString()
                                newMessages.add(MessageTypeClass(nameOfUser, imgAvaOfChatURL, numberOfUser, id))
                            }
                        } else {
                            for(snap1 in snap.child("listsUser").children) {
                                val nameOfUser = snap1.child("nameOfChat").value.toString()
                                val imgAvaOfChatURL = snap1.child("imgAvaOfChatURL").value.toString()
                                messageGroupList.add(MessageTypeClassGroup(nameOfUser, imgAvaOfChatURL))
                            }
                        }
                    } else if(string == "Все") {
                        for(snap1 in snap.child("listsUser").children) {
                            if(snap1.child("number").value.toString() != "null") {
                                Log.e("numbers", snap1.child("number").value.toString())
                                Log.e("numbers1", snap1.child("nameOfChat").value.toString())
                                val nameOfUser = snap1.child("nameOfChat").value.toString()
                                val imgAvaOfChatURL = snap1.child("imgAvaOfChatURL").value.toString()
                                val numberOfUser = snap1.child("number").value.toString()
                                val id = snap1.child("id").value.toString()
                                if(newMessages.size != 0) {
                                    val isDuplicate = newMessages.any {
                                        it.nameOfChat == nameOfUser && it.imgAvaOfChatURL == imgAvaOfChatURL
                                    }

                                    if (!isDuplicate) {
                                        newMessages.add(MessageTypeClass(nameOfUser, imgAvaOfChatURL, numberOfUser, id))
                                        Log.e("wtf", newMessages.toString())
                                    }
                                } else {
                                    newMessages.add(MessageTypeClass(nameOfUser, imgAvaOfChatURL, numberOfUser, id))
                                }
                            } else {
                                for(snap1 in snap.child("listsUser").children) {
                                    Log.e("numbers2", snap1.child("number").value.toString())
                                    Log.e("numbers3", snap1.child("nameOfChat").value.toString())
                                    val nameOfUser = snap1.child("nameOfChat").value.toString()
                                    val imgAvaOfChatURL = snap1.child("imgAvaOfChatURL").value.toString()
                                    if(messageGroupList.size != 0) {
                                        for(i in messageGroupList.indices) {
                                            if((imgAvaOfChatURL != "" || nameOfUser != "") && (!messageGroupList[i].nameOfChat.contains(nameOfUser) || !messageGroupList[i].imgAvaOfChatURL.contains(imgAvaOfChatURL))) {
                                                messageGroupList.add(MessageTypeClassGroup(nameOfUser, imgAvaOfChatURL))
                                                Log.e("numbers8", messageGroupList.toString())
                                            }
                                        }
                                    } else {
                                        messageGroupList.add(MessageTypeClassGroup(nameOfUser, imgAvaOfChatURL))
                                        Log.e("numbers7", messageGroupList.toString())
                                    }
                                }
                            }
                        }

                    }
                }
                val adapter = TabOfMessangesAdapter(newMessages, navController, textSizeOfMessange!!)
                binding.rvTabOfMessanges.adapter = adapter
                if(messageGroupList.size != 0) {
                    val adapter1 = TabOfMessangesGroupAdapter(messageGroupList, navController, textSizeOfMessange!!)
                    binding.rvTabOfMessangesGroup.adapter = adapter1
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}