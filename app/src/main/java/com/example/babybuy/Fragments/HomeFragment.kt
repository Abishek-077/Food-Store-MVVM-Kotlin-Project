import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.babybuy.Dashboard
import com.example.babybuy.Fragments.ShopFragment
import com.example.babybuy.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Find buttons by ID
        val exploreButton = view.findViewById<Button>(R.id.explore_btn)
        val shopButton = view.findViewById<Button>(R.id.shop_btn)

        // Set up click listeners
        exploreButton.setOnClickListener {
            // Navigate to ShopFragment
            navigateToFragment(ShopFragment.newInstance())
        }

        shopButton.setOnClickListener {
            // Navigate to another fragment or perform an action
            navigateToFragment(ShopFragment.newInstance())
        }

        return view
    }

    private fun navigateToFragment(fragment: Fragment) {
        val activity = activity as? AppCompatActivity
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fcv_dashboard, fragment)
            ?.addToBackStack(null) // Optional: Add to back stack to allow users to navigate back
            ?.commit()

        val dashActivity = activity as? Dashboard
        dashActivity?.updateBottomNavigationView(fragment)
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
