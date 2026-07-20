package ir.factory.entryexit.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ir.factory.entryexit.databinding.FragmentCategoryBinding
import ir.factory.entryexit.data.PersonEntity
import ir.factory.entryexit.ui.GroupedPersonAdapter
import ir.factory.entryexit.viewmodel.FactoryViewModel

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: FactoryViewModel by activityViewModels()
    private lateinit var adapter: GroupedPersonAdapter
    private var categoryName: String = ""

    companion object {
        private const val ARG_CATEGORY = "category_name"

        fun newInstance(category: String): CategoryFragment {
            val fragment = CategoryFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryName = arguments?.getString(ARG_CATEGORY) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // متغیر کنترل کلیک برای ممانعت از ثبت تکراری به دلیل کلیک‌های سریع و پیاپی نگهبان
        var lastClickTime: Long = 0

        adapter = GroupedPersonAdapter(
            onItemClick = { person ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 800) {
                    return@GroupedPersonAdapter // ممانعت از دبل‌کلیک ناخواسته
                }
                lastClickTime = currentTime

                // ثبت تردد بدون دیالوگ آزاردهنده "آیا مطمئن هستید؟" به صورت مستقیم و آنی
                if (person.isInside) {
                    viewModel.checkOut(person)
                } else {
                    viewModel.checkIn(person)
                }
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // مانیتورینگ زنده داده‌ها از طریق لایو دیتای متصل به دیتابیس محلی همگام‌شده
        viewModel.getPeopleByCategory(categoryName).observe(viewLifecycleOwner) { people ->
            adapter.submitList(people)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
