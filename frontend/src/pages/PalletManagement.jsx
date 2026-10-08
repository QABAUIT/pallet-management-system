import React from "react";
import { Skeleton, Flex, Typography } from "antd";
import Pagination from "../components/common/Pagination";
import PalletHeader from "../components/pallet/PalletHeader";
import PalletCard from "../components/pallet/PalletCard";
import PalletDetailDrawer from "../components/pallet/PalletDetailDrawer";
import PalletModal from "../components/pallet/PalletModal";
import { usePallets } from "../hooks/usePallets";
import { exportToCsv } from "../utils/exportCsv";

const { Title, Text } = Typography;

export default function PalletManagement() {
  const {
    loading,
    searchText,
    setSearchText,
    filter,
    setFilter,
    filterTab,
    setFilterTab,
    currentPage,
    setCurrentPage,
    paginatedPallets,
    filteredPallets,
    totalItems,
    selectedPallet,
    setSelectedPallet,
    isModalVisible,
    setIsModalVisible,
    modalMode,
    form,
    handleAdd,
    handleEdit,
    handleDelete,
    handleModalSubmit,
  } = usePallets(8);

  const defaultFilter = { loaiMatHang: [], chatLieu: [] };

  const handleExport = () => {
    // Export the filtered list, not just the current page
    exportToCsv(`Danh_sach_pallet_${new Date().getTime()}.csv`, filteredPallets);
  };

  return (
    <Flex vertical style={{ padding: 24, minHeight: "calc(100vh - 64px)" }}>
      <PalletHeader
        searchText={searchText}
        setSearchText={setSearchText}
        filter={filter}
        setFilter={setFilter}
        filterTab={filterTab}
        setFilterTab={setFilterTab}
        defaultFilter={defaultFilter}
        totalPallets={totalItems}
        onAdd={handleAdd}
        onExport={handleExport}
      />

      <Flex vertical style={{ flex: 1 }}>
        <Flex align="center" gap="small" style={{ marginBottom: 16 }}>
          <Text strong style={{ fontSize: 14 }}>
            DANH MỤC SẢN PHẨM
          </Text>
          <Text
            style={{
              background: "#e2e8f0",
              padding: "2px 8px",
              borderRadius: 12,
              fontSize: 12,
              color: "#64748b",
            }}
          >
            Hiển thị {paginatedPallets.length}/{totalItems}
          </Text>
          <Text type="secondary" style={{ marginLeft: "auto", fontSize: 13 }}>
            💡 Bấm thẻ để xem chi tiết đầy đủ bên phải
          </Text>
        </Flex>

        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))",
            gap: 16,
            marginBottom: 24,
          }}
        >
          {loading ? (
            <Skeleton active paragraph={{ rows: 4 }} />
          ) : (
            paginatedPallets.map((pallet) => (
              <PalletCard
                key={pallet.id}
                pallet={pallet}
                isSelected={selectedPallet?.id === pallet.id}
                onClick={() =>
                  setSelectedPallet(
                    selectedPallet?.id === pallet.id ? null : pallet,
                  )
                }
              />
            ))
          )}
        </div>

        <div style={{ width: "100%", marginTop: "16px" }}>
          <Pagination
            currentPage={currentPage}
            totalItems={totalItems}
            itemsPerPage={8}
            itemLabel="pallet"
            onPageChange={setCurrentPage}
          />
        </div>

        <PalletDetailDrawer
          selectedPallet={selectedPallet}
          onClose={() => setSelectedPallet(null)}
          onEdit={() => handleEdit(selectedPallet)}
          onDelete={() => handleDelete(selectedPallet.id)}
        />

        <PalletModal
          isModalVisible={isModalVisible}
          setIsModalVisible={setIsModalVisible}
          modalMode={modalMode}
          form={form}
          handleModalSubmit={handleModalSubmit}
        />
      </Flex>
    </Flex>
  );
}
