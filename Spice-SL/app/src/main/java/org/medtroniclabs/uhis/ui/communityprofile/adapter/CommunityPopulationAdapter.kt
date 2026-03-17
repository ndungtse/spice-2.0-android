package org.medtroniclabs.uhis.ui.communityprofile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.medtroniclabs.uhis.data.community.CommunityPopulation
import org.medtroniclabs.uhis.databinding.ItemCommunityPopulationBinding

class CommunityPopulationAdapter :
    RecyclerView.Adapter<CommunityPopulationAdapter.CommunityPopulationViewHolder>() {
    private val communityPopulationList = mutableListOf<CommunityPopulation>()

    inner class CommunityPopulationViewHolder(
        private val binding: ItemCommunityPopulationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(population: CommunityPopulation) {
            binding.tvCategory.text = population.title
            binding.tvCount.text = population.count.toString()
            if (bindingAdapterPosition == communityPopulationList.size - 1) {
                binding.viewBottomDivider.visibility = View.GONE
            } else {
                binding.viewBottomDivider.visibility = View.VISIBLE
            }
        }
    }

    fun updateList(newCommunityPopulationList: List<CommunityPopulation>) {
        val oldCount = newCommunityPopulationList.size
        communityPopulationList.clear()
        communityPopulationList.addAll(newCommunityPopulationList)
        notifyItemRangeRemoved(0, oldCount)
        notifyItemRangeInserted(0, newCommunityPopulationList.size)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CommunityPopulationViewHolder =
        CommunityPopulationViewHolder(
            ItemCommunityPopulationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun getItemCount(): Int = communityPopulationList.size

    override fun onBindViewHolder(
        holder: CommunityPopulationViewHolder,
        position: Int,
    ) {
        holder.bind(communityPopulationList[position])
    }
}
