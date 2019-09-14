package com.example.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.HashMap
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.app_bar_main.fab
import kotlinx.android.synthetic.main.content_main.listView
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.internal.FirebaseAppHelper.getUid




class QuestionDetailActivity : AppCompatActivity() {

    private var flag = false

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }



            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()




        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        val database = FirebaseDatabase.getInstance()


        val userId = FirebaseAuth.getInstance().currentUser!!.uid




        FirebaseDatabase.getInstance().reference.child("favorite").child(userId).child(mQuestion.questionUid).child("genre").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get user value
                    val user = dataSnapshot.getValue(userId::class.java)

                   if(user == null) {
                       flag = true


                       val buttonTextView = button.findViewById<View>(R.id.button) as Button

                       buttonTextView.text = ("お気に入り")

                       Log.d("TAG", "まだお気に入りにしていない")
                   }else{
                       flag = false

                       val buttonTextView = button.findViewById<View>(R.id.button) as Button
                       buttonTextView.text = ("お気に入り済み")

                       Log.d("TAG","お気に入り済み")
                   }

                }

                override fun onCancelled(databaseError: DatabaseError) {



                }
            })


        button.setOnClickListener {

            val database = FirebaseDatabase.getInstance()




            val ref = database.getReference("favorite")

            val data = HashMap<String, String>()




            if(flag){


                val buttonTextView = button.findViewById<View>(R.id.button) as Button

                buttonTextView.text = ("お気に入り")
                flag = false




                // ログイン済みのユーザーを取得する
                val user = FirebaseAuth.getInstance().currentUser

                // UID
                data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid

                val userId = FirebaseAuth.getInstance().currentUser!!.uid

                ref.child(userId).child(mQuestion.questionUid).child("genre").removeValue()





            }else{

                val buttonTextView = button.findViewById<View>(R.id.button) as Button
                buttonTextView.text = ("お気に入り済み")
                flag = true




                // ログイン済みのユーザーを取得する
                val user = FirebaseAuth.getInstance().currentUser

                // UID
                data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid

                val userId = FirebaseAuth.getInstance().currentUser!!.uid

                ref.child(userId).child(mQuestion.questionUid).child("genre").setValue(mQuestion.genre.toString())



            }




        }



        val user = FirebaseAuth.getInstance().currentUser

        if(user == null ){
            button.setVisibility(View.INVISIBLE) // 表示しない


        }
        else{
            button.setVisibility(View.VISIBLE) // 表示


        }



        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
    }
}