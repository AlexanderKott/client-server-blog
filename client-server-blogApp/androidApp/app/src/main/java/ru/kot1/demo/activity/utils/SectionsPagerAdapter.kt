package ru.kot1.demo.activity.utils

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.kot1.demo.R
import ru.kot1.demo.activity.pages.*

  val  TAB_TITLES = arrayOf(
    R.string.tab_text_0,
    R.string.tab_text_1,
    R.string.tab_text_2,
    R.string.tab_text_3,
    R.string.tab_text_4,
)

val  TAB_ICONS = arrayOf(
    R.drawable.ic_baseline_user_placeholder,
    R.drawable.users,
    R.drawable.posts,
    R.drawable.events,
    R.drawable.ic_about,
)




class SectionsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment {
          return when (position) {
           0 -> MyPageFragment()
           1 -> UsersFragment()
           2 -> PostsAllFragment()
           3 -> EventsAllFragment()
           4 -> AboutFragment()
           else -> throw  UnsupportedClassVersionError()
       }
    }

    override fun getItemCount(): Int {
        return TAB_TITLES.size
    }
}