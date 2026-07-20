package ir.factory.entryexit.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.factory.entryexit.databinding.ItemRosterEntryBinding
import ir.factory.entryexit.data.PersonEntity
import android.graphics.Color

class GroupedPersonAdapter(
    private val onItemClick: (PersonEntity) -> Unit
) : ListAdapter<PersonEntity, GroupedPersonAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRosterEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemRosterEntryBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(person: PersonEntity) {
            // نمایش نام و مشخصات مراجع
            binding.tvName.text = person.name
            binding.tvIdAndCategory.text = "${person.id} | ${person.category}"
            
            // تنظیم استایل دکمه تک‌کلیکی بر اساس وضعیت حضور (بدون نیاز به دیالوگ تایید)
            if (person.isInside) {
                binding.btnToggleState.text = "ثبت خروج"
                binding.btnToggleState.setBackgroundColor(Color.parseColor("#ef4444")) // رنگ قرمز ممتاز برای خروج
                binding.btnToggleState.setTextColor(Color.WHITE)
            } else {
                binding.btnToggleState.text = "ثبت ورود"
                binding.btnToggleState.setBackgroundColor(Color.parseColor("#10b981")) // رنگ سبز درخشان برای ورود
                binding.btnToggleState.setTextColor(Color.WHITE)
            }

            // شنود کلیک برای تغییر فوری وضعیت
            binding.btnToggleState.setOnClickListener {
                onItemClick(person)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PersonEntity>() {
        override fun areItemsTheSame(oldItem: PersonEntity, newItem: PersonEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PersonEntity, newItem: PersonEntity) = oldItem == newItem
    }
}
