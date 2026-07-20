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
import ir.factory.entryexit.data.PersonType
import ir.factory.entryexit.ui.GroupedPersonAdapter
import ir.factory.entryexit.viewmodel.FactoryViewModel

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: FactoryViewModel by activityViewModels()
    private lateinit var adapter: GroupedPersonAdapter
    private lateinit var personType: PersonType

    companion object {
        private const val ARG_TYPE = "person_type"

        fun newInstance(type: PersonType): CategoryFragment {
            val fragment = CategoryFragment()
            val args = Bundle()
            args.putSerializable(ARG_TYPE, type)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        personType = arguments?.getSerializable(ARG_TYPE) as? PersonType ?: PersonType.PERSONNEL
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

        adapter = GroupedPersonAdapter { person ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 800) {
                return@GroupedPersonAdapter // ممانعت از دبل‌کلیک ناخواسته
            }
            lastClickTime = currentTime

            // ثبت تردد بدون دیالوگ آزاردهنده "آیا مطمئن هستید؟" به صورت مستقیم و آنی با تکیه بر آداپتور اصلی پروژه
            if (person.isInside) {
                viewModel.checkOut(person.id) { }
            } else {
                viewModel.checkIn(person.id) { }
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // مانیتورینگ زنده داده‌ها از طریق لایو دیتای متصل به دیتابیس محلی همگام‌شده
        viewModel.getPersonsByType(personType).observe(viewLifecycleOwner) { people ->
            adapter.submitList(people)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
